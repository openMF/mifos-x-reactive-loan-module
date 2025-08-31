package org.mifos.loanrisk.external.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import org.mifos.loanrisk.document.storage.ObjectStorageClient;
import org.mifos.loanrisk.external.bsa.domain.BankStatementAnalysisResult;
import org.mifos.loanrisk.external.bsa.repository.BankStatementAnalysisResultRepository;
import org.mifos.loanrisk.external.cb.domain.CreditBureauResult;
import org.mifos.loanrisk.external.cb.repository.CreditBureauResultRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AnalysisResultUIController {

    private final BankStatementAnalysisResultRepository bsaRepository;
    private final CreditBureauResultRepository cbRepository;
    private final ObjectMapper objectMapper;
    private final ObjectStorageClient storageClient;

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

