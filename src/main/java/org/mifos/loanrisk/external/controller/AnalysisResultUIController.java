package org.mifos.loanrisk.external.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import org.mifos.loanrisk.document.storage.ObjectStorageClient;
import org.mifos.loanrisk.external.bsa.domain.BankStatementAnalysisResult;
import org.mifos.loanrisk.external.bsa.repository.BankStatementAnalysisResultRepository;
import org.mifos.loanrisk.external.cb.domain.CreditBureauResult;
import org.mifos.loanrisk.external.cb.repository.CreditBureauResultRepository;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AnalysisResultUIController {

    private final BankStatementAnalysisResultRepository bsaRepository;
    private final CreditBureauResultRepository cbRepository;
    private final ObjectMapper objectMapper;
    private final ObjectStorageClient storageClient;
    private final LoanSnapshotRepository loanSnapshotRepository;

    @GetMapping(value = "/analysis/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> getDashboardPage() {
        return loadDashboardRows().map(this::renderDashboardHtml);
    }

    @GetMapping(value = "/analysis/dashboard/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<DashboardRow>> getDashboardData() {
        return loadDashboardRows();
    }

    @GetMapping(value = "/bsa", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> getAllBsaResults() {
        return bsaRepository.findAll()
                .map(this::toPrettyJson)
                .collectList()
                .map(this::asHtml);
    }

    @GetMapping(value = "/bsa/loan/{loanId}", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> getBsaResultsByLoan(@PathVariable("loanId") Long loanId) {
        return bsaRepository.findAllByLoanId(loanId)
                .map(this::toPrettyJson)
                .collectList()
                .map(this::asHtml);
    }

    @GetMapping(value = "/bsa/report/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> getBsaReport(@PathVariable("id") Long id) {
        return bsaRepository.findById(id)
                .flatMap(result -> {
                    if (result.getAttributes() == null) {
                        return Mono.empty();
                    }
                    try {
                        String reportKey = objectMapper.readTree(result.getAttributes().asString())
                                .path("reportKey").asText(null);
                        if (reportKey == null || reportKey.isBlank()) {
                            return Mono.empty();
                        }
                        return storageClient.get(reportKey)
                                .map(data -> ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"bsa-report-" + id + ".pdf\"")
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .body(data));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    @GetMapping(value = "/cb", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> getAllCbResults() {
        return cbRepository.findAll()
                .map(this::toPrettyJson)
                .collectList()
                .map(this::asHtml);
    }

    @GetMapping(value = "/cb/loan/{loanId}", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> getCbResultsByLoan(@PathVariable("loanId") Long loanId) {
        return cbRepository.findAllByLoanId(loanId)
                .map(this::toPrettyJson)
                .collectList()
                .map(this::asHtml);
    }

    private Mono<List<DashboardRow>> loadDashboardRows() {
        Mono<Map<Long, BankStatementAnalysisResult>> bsaMono = bsaRepository.findAll()
                .collectList()
                .map(this::latestBsaByLoan);

        Mono<Map<Long, CreditBureauResult>> cbMono = cbRepository.findAll()
                .collectList()
                .map(this::latestCbByLoan);

        return Mono.zip(bsaMono, cbMono)
                .flatMap(tuple -> {
                    Map<Long, BankStatementAnalysisResult> bsaMap = tuple.getT1();
                    Map<Long, CreditBureauResult> cbMap = tuple.getT2();
                    if (bsaMap.isEmpty() && cbMap.isEmpty()) {
                        return Mono.just(List.of());
                    }
                    Set<Long> loanIds = new TreeSet<>();
                    loanIds.addAll(bsaMap.keySet());
                    loanIds.addAll(cbMap.keySet());

                    return Flux.fromIterable(loanIds)
                            .flatMap(loanId -> buildDashboardRow(loanId, bsaMap.get(loanId), cbMap.get(loanId)))
                            .collectList()
                            .map(list -> {
                                list.sort(Comparator.comparing(DashboardRow::getLoanId));
                                return list;
                            });
                });
    }

    private Mono<DashboardRow> buildDashboardRow(Long loanId, BankStatementAnalysisResult bsa, CreditBureauResult cb) {
        ResultStatusDto bsaStatus = parseBsaStatus(loanId, bsa);
        ResultStatusDto cbStatus = parseCreditBureauStatus(cb);
        return loanSnapshotRepository.findByLoanId(loanId)
                .map(snapshot -> extractClientName(snapshot.getPayload()))
                .onErrorReturn("Unknown Client")
                .defaultIfEmpty("Unknown Client")
                .map(clientName -> new DashboardRow(loanId, clientName, bsaStatus, cbStatus));
    }

    private ResultStatusDto parseBsaStatus(Long loanId, BankStatementAnalysisResult result) {
        if (result == null) {
            return ResultStatusDto.unavailable();
        }
        Json attributes = result.getAttributes();
        if (attributes == null) {
            return ResultStatusDto.failure();
        }
        try {
            JsonNode node = objectMapper.readTree(attributes.asString());
            boolean success = node.path("success").asBoolean(false);
            if (success) {
                return ResultStatusDto.success("/bsa/loan/" + loanId);
            }
            return ResultStatusDto.failure();
        } catch (JsonProcessingException e) {
            return ResultStatusDto.failure();
        }
    }

    private ResultStatusDto parseCreditBureauStatus(CreditBureauResult result) {
        if (result == null) {
            return ResultStatusDto.unavailable();
        }
        Json attributes = result.getAttributes();
        if (attributes == null) {
            return ResultStatusDto.failure();
        }
        try {
            JsonNode node = objectMapper.readTree(attributes.asString());
            String link = extractCreditBureauLink(node);
            if (link != null && !link.isBlank()) {
                return ResultStatusDto.success(link);
            }
            return ResultStatusDto.failure();
        } catch (JsonProcessingException e) {
            return ResultStatusDto.failure();
        }
    }

    private Map<Long, BankStatementAnalysisResult> latestBsaByLoan(List<BankStatementAnalysisResult> results) {
        Map<Long, BankStatementAnalysisResult> map = new HashMap<>();
        for (BankStatementAnalysisResult result : results) {
            if (result == null || result.getLoanId() == null) {
                continue;
            }
            map.merge(result.getLoanId(), result, this::selectLatest);
        }
        return map;
    }

    private Map<Long, CreditBureauResult> latestCbByLoan(List<CreditBureauResult> results) {
        Map<Long, CreditBureauResult> map = new HashMap<>();
        for (CreditBureauResult result : results) {
            if (result == null || result.getLoanId() == null) {
                continue;
            }
            map.merge(result.getLoanId(), result, this::selectLatest);
        }
        return map;
    }

    private <T> T selectLatest(T current, T next) {
        Long currentId = extractId(current);
        Long nextId = extractId(next);
        if (nextId == null) {
            return current;
        }
        if (currentId == null || nextId > currentId) {
            return next;
        }
        return current;
    }

    private Long extractId(Object value) {
        if (value instanceof BankStatementAnalysisResult bsa) {
            return bsa.getId();
        }
        if (value instanceof CreditBureauResult cb) {
            return cb.getId();
        }
        return null;
    }

    private String extractClientName(String payload) {
        if (payload == null || payload.isBlank()) {
            return "Unknown Client";
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            String clientName = textOrNull(root.path("clientName"));
            if (clientName == null) {
                clientName = textOrNull(root.path("client").path("displayName"));
            }
            if (clientName == null) {
                clientName = textOrNull(root.path("client").path("name"));
            }
            if (clientName == null) {
                JsonNode clientNode = root.path("client");
                String first = textOrNull(clientNode.path("firstName"));
                String last = textOrNull(clientNode.path("lastName"));
                if (first != null || last != null) {
                    StringBuilder builder = new StringBuilder();
                    if (first != null) {
                        builder.append(first);
                    }
                    if (last != null) {
                        if (builder.length() > 0) {
                            builder.append(' ');
                        }
                        builder.append(last);
                    }
                    clientName = builder.toString();
                }
            }
            return clientName != null && !clientName.isBlank() ? clientName : "Unknown Client";
        } catch (JsonProcessingException e) {
            return "Unknown Client";
        }
    }

    private String textOrNull(JsonNode node) {
        if (node != null && node.isTextual()) {
            String value = node.asText();
            if (!value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String extractCreditBureauLink(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode linkNode = node.get("link");
        if (linkNode != null && linkNode.isTextual() && !linkNode.asText().isBlank()) {
            return linkNode.asText();
        }
        JsonNode reportsNode = node.get("reports");
        if (reportsNode != null) {
            String link = extractCreditBureauLink(reportsNode);
            if (link != null) {
                return link;
            }
        }
        JsonNode attributesNode = node.get("attributes");
        if (attributesNode != null) {
            String link = extractCreditBureauLink(attributesNode);
            if (link != null) {
                return link;
            }
        }
        if (node.isArray()) {
            for (JsonNode element : node) {
                String link = extractCreditBureauLink(element);
                if (link != null) {
                    return link;
                }
            }
        } else if (node.isObject()) {
            for (JsonNode child : node) {
                String link = extractCreditBureauLink(child);
                if (link != null) {
                    return link;
                }
            }
        }
        return null;
    }

    private String renderDashboardHtml(List<DashboardRow> rows) {
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            dataJson = "[]";
        }
        String template = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Loan Assessment results dashboard</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            margin: 0;
                            padding: 0;
                            background-color: #f4f6f8;
                            color: #1f2933;
                        }
                        .container {
                            max-width: 1200px;
                            margin: 40px auto;
                            background: #ffffff;
                            border-radius: 12px;
                            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.1);
                            padding: 32px;
                        }
                        h1 {
                            margin-top: 0;
                            font-size: 28px;
                            text-align: center;
                            color: #0f172a;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 24px;
                        }
                        thead {
                            background-color: #0f172a;
                            color: #ffffff;
                        }
                        th, td {
                            padding: 14px 18px;
                            text-align: left;
                            border-bottom: 1px solid #e5e7eb;
                        }
                        tbody tr:hover {
                            background-color: #f1f5f9;
                        }
                        .status-btn {
                            padding: 8px 16px;
                            border: none;
                            border-radius: 9999px;
                            font-weight: 600;
                            cursor: pointer;
                            transition: transform 0.2s ease, box-shadow 0.2s ease;
                        }
                        .status-btn.success {
                            background-color: #16a34a;
                            color: #ffffff;
                        }
                        .status-btn.failure {
                            background-color: #dc2626;
                            color: #ffffff;
                        }
                        .status-btn.unavailable {
                            background-color: #6b7280;
                            color: #ffffff;
                            cursor: not-allowed;
                        }
                        .status-btn.success:hover {
                            transform: translateY(-1px);
                            box-shadow: 0 6px 14px rgba(22, 163, 74, 0.35);
                        }
                        .status-btn.failure:hover {
                            transform: translateY(-1px);
                            box-shadow: 0 6px 14px rgba(220, 38, 38, 0.35);
                        }
                        .status-btn[disabled] {
                            opacity: 0.7;
                            cursor: not-allowed;
                            box-shadow: none;
                            transform: none;
                        }
                        .pagination {
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            margin-top: 24px;
                            gap: 12px;
                        }
                        .pagination button {
                            padding: 8px 18px;
                            border-radius: 9999px;
                            border: none;
                            background-color: #1d4ed8;
                            color: #ffffff;
                            font-weight: 600;
                            cursor: pointer;
                            transition: background-color 0.2s ease, box-shadow 0.2s ease;
                        }
                        .pagination button:disabled {
                            background-color: #94a3b8;
                            cursor: not-allowed;
                            box-shadow: none;
                        }
                        .pagination button:hover:not(:disabled) {
                            background-color: #1e40af;
                            box-shadow: 0 6px 14px rgba(29, 78, 216, 0.35);
                        }
                        #pageInfo {
                            font-weight: 600;
                            color: #475569;
                        }
                        .empty-state {
                            text-align: center;
                            padding: 32px 0;
                            color: #6b7280;
                            font-size: 18px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Loan Assessment results dashboard</h1>
                        <div class="table-wrapper">
                            <table id="resultsTable">
                                <thead>
                                    <tr>
                                        <th>Loan ID</th>
                                        <th>Client Name</th>
                                        <th>Bank Statement Analysis</th>
                                        <th>Credit Bureau</th>
                                    </tr>
                                </thead>
                                <tbody></tbody>
                            </table>
                            <div id="emptyState" class="empty-state" style="display: none;">
                                No assessment results to display yet.
                            </div>
                        </div>
                        <div class="pagination">
                            <button id="prevPage" type="button">Previous</button>
                            <span id="pageInfo"></span>
                            <button id="nextPage" type="button">Next</button>
                        </div>
                    </div>
                    <script>
                        const tableData = {{DATA}};
                        const rowsPerPage = 10;
                        let currentPage = 1;

                        const tableElement = document.getElementById('resultsTable');
                        const tbody = tableElement.querySelector('tbody');
                        const emptyState = document.getElementById('emptyState');
                        const prevBtn = document.getElementById('prevPage');
                        const nextBtn = document.getElementById('nextPage');
                        const pageInfo = document.getElementById('pageInfo');

                        function createStatusButton(status) {
                            const state = status && status.state ? status.state : 'UNAVAILABLE';
                            const label = status && status.label ? status.label : (state === 'SUCCESS' ? 'Success' : 'Not Available');
                            const btn = document.createElement('button');
                            btn.type = 'button';
                            btn.classList.add('status-btn', state.toLowerCase());
                            btn.textContent = label;

                            if (status && status.clickable && status.url) {
                                btn.addEventListener('click', () => {
                                    window.open(status.url, '_blank');
                                });
                            } else {
                                btn.disabled = true;
                            }
                            return btn;
                        }

                        function renderTable() {
                            const totalRows = tableData.length;
                            const totalPages = Math.max(1, Math.ceil(totalRows / rowsPerPage));

                            if (currentPage > totalPages) {
                                currentPage = totalPages;
                            }

                            tbody.innerHTML = '';

                            if (totalRows === 0) {
                                tableElement.style.display = 'none';
                                emptyState.style.display = 'block';
                                pageInfo.textContent = 'Page 0 of 0';
                                prevBtn.disabled = true;
                                nextBtn.disabled = true;
                                return;
                            }

                            tableElement.style.display = 'table';
                            emptyState.style.display = 'none';

                            const start = (currentPage - 1) * rowsPerPage;
                            const end = Math.min(start + rowsPerPage, totalRows);

                            for (let i = start; i < end; i++) {
                                const row = tableData[i];
                                const tr = document.createElement('tr');

                                const loanCell = document.createElement('td');
                                loanCell.textContent = (row && row.loanId != null) ? row.loanId : '';
                                tr.appendChild(loanCell);

                                const clientCell = document.createElement('td');
                                clientCell.textContent = row.clientName || 'Unknown Client';
                                tr.appendChild(clientCell);

                                const bsaCell = document.createElement('td');
                                bsaCell.appendChild(createStatusButton(row.bsa));
                                tr.appendChild(bsaCell);

                                const cbCell = document.createElement('td');
                                cbCell.appendChild(createStatusButton(row.creditBureau));
                                tr.appendChild(cbCell);

                                tbody.appendChild(tr);
                            }

                            pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
                            prevBtn.disabled = currentPage === 1;
                            nextBtn.disabled = currentPage === totalPages;
                        }

                        prevBtn.addEventListener('click', () => {
                            if (currentPage > 1) {
                                currentPage -= 1;
                                renderTable();
                            }
                        });

                        nextBtn.addEventListener('click', () => {
                            const totalPages = Math.max(1, Math.ceil(tableData.length / rowsPerPage));
                            if (currentPage < totalPages) {
                                currentPage += 1;
                                renderTable();
                            }
                        });

                        renderTable();
                    </script>
                </body>
                </html>
                """;
        return template.replace("{{DATA}}", dataJson);
    }

    private static final class DashboardRow {
        private final Long loanId;
        private final String clientName;
        private final ResultStatusDto bsa;
        private final ResultStatusDto creditBureau;

        private DashboardRow(Long loanId, String clientName, ResultStatusDto bsa, ResultStatusDto creditBureau) {
            this.loanId = loanId;
            this.clientName = clientName;
            this.bsa = bsa;
            this.creditBureau = creditBureau;
        }

        public Long getLoanId() {
            return loanId;
        }

        public String getClientName() {
            return clientName;
        }

        public ResultStatusDto getBsa() {
            return bsa;
        }

        public ResultStatusDto getCreditBureau() {
            return creditBureau;
        }
    }

    private static final class ResultStatusDto {
        private final String state;
        private final String label;
        private final String url;
        private final boolean clickable;

        private ResultStatusDto(String state, String label, String url, boolean clickable) {
            this.state = state;
            this.label = label;
            this.url = url;
            this.clickable = clickable;
        }

        public static ResultStatusDto success(String url) {
            return new ResultStatusDto("SUCCESS", "Success", url, true);
        }

        public static ResultStatusDto failure() {
            return new ResultStatusDto("FAILURE", "Failure", null, false);
        }

        public static ResultStatusDto unavailable() {
            return new ResultStatusDto("UNAVAILABLE", "Not Available", null, false);
        }

        public String getState() {
            return state;
        }

        public String getLabel() {
            return label;
        }

        public String getUrl() {
            return url;
        }

        public boolean isClickable() {
            return clickable;
        }
    }

    private String toPrettyJson(BankStatementAnalysisResult result) {
        return formatResult(result.getId(), result.getLoanId(), result.getAttributes());
    }

    private String toPrettyJson(CreditBureauResult result) {
        return formatResult(result.getId(), result.getLoanId(), result.getAttributes());
    }

    private String formatResult(Long id, Long loanId, Json attributes) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("loanId", loanId);
        if (attributes != null) {
            try {
                node.set("attributes", objectMapper.readTree(attributes.asString()));
            } catch (JsonProcessingException e) {
                node.put("attributes", attributes.asString());
            }
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String asHtml(List<String> jsons) {
        String joined = jsons.stream().collect(Collectors.joining("\n\n"));
        return "<html><body><pre>" + joined + "</pre></body></html>";
    }
}

