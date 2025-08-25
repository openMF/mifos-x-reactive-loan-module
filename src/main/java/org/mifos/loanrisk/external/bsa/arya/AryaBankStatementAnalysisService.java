package org.mifos.loanrisk.external.bsa.arya;

import java.util.Base64;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.mifos.loanrisk.common.ServiceStatus;
import org.mifos.loanrisk.domain.Aggregator;
import org.mifos.loanrisk.document.domain.DocumentMeta;
import org.mifos.loanrisk.document.service.fetch.DocumentFetchService;
import org.mifos.loanrisk.document.storage.ObjectStorageClient;
import org.mifos.loanrisk.external.bsa.BankStatementAnalysisService;
import org.mifos.loanrisk.external.bsa.domain.BankStatementAnalysisResult;
import org.mifos.loanrisk.external.bsa.repository.BankStatementAnalysisResultRepository;
import org.mifos.loanrisk.repository.AggregatorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Bank statement analysis implementation using Arya's API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AryaBankStatementAnalysisService implements BankStatementAnalysisService {

    private final DocumentFetchService documentFetchService;
    private final ObjectStorageClient storageClient;
    private final AggregatorRepository aggregatorRepository;
    private final BankStatementAnalysisResultRepository resultRepository;
    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${bsa.arya.base-url:https://ping.arya.ai/api/v1}")
    private String baseUrl;

    @Value("${bsa.arya.token:}")
    private String token;

    @Override
    public String getName() {
        return "arya";
    }

    @Override
    public Mono<Void> analyze(DocumentDataV1 document, Aggregator aggregator) {
        return documentFetchService.fetch(document.getParentEntityType(), document.getParentEntityId(), document.getId())
                .flatMap((DocumentMeta meta) -> storageClient.get(meta.getObjectKey()))
                .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                .flatMap(base64 -> sendRequest(base64, document, aggregator));
    }

    private Mono<Void> sendRequest(String base64, DocumentDataV1 document, Aggregator aggregator) {
        String reqId = UUID.randomUUID().toString();
        AryaBsaRequest body = new AryaBsaRequest("pdf", reqId, "pdf", base64);
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        return client.post().uri("/bank-statement-analyser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AryaBsaResponse.class)
                .flatMap(resp -> saveResult(resp, aggregator))
                .doOnError(err -> log.error("Arya BSA request failed", err))
                .then(updateAggregatorStatus(aggregator));
    }

    private Mono<BankStatementAnalysisResult> saveResult(AryaBsaResponse resp, Aggregator aggregator) {
        try {
            String json = mapper.writeValueAsString(resp);
            BankStatementAnalysisResult result = new BankStatementAnalysisResult(null, aggregator.getLoanId(), json);
            return resultRepository.save(result);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> updateAggregatorStatus(Aggregator aggregator) {
        aggregator.setBankStmtStatus(ServiceStatus.REQUESTED);
        return aggregatorRepository.save(aggregator).then();
    }
}

