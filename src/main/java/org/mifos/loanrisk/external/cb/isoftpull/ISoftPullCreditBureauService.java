package org.mifos.loanrisk.external.cb.isoftpull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.loanrisk.common.ServiceStatus;
import org.mifos.loanrisk.domain.Aggregator;
import org.mifos.loanrisk.external.cb.CreditBureauService;
import org.mifos.loanrisk.external.cb.domain.CreditBureauResult;
import org.mifos.loanrisk.external.cb.repository.CreditBureauResultRepository;
import org.mifos.loanrisk.repository.AggregatorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ISoftPullCreditBureauService implements CreditBureauService {

    private final CreditBureauResultRepository resultRepository;
    private final AggregatorRepository aggregatorRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;
    private final ISoftPullRequestMapper requestMapper;

    @Value("${cb.isoftpull.base-url:https://api.isoftpull.com}")
    private String baseUrl;

    @Value("${cb.isoftpull.api-key:}")
    private String apiKey;

    @Value("${cb.isoftpull.api-secret:}")
    private String apiSecret;

    @Override
    public String getName() {
        return "isoftpull";
    }

    @Override
    public Mono<Void> pull(Aggregator aggregator) {
        return requestMapper.map(aggregator.getLoanId())
                .flatMap(body -> sendRequest(body, aggregator));
    }

    private Mono<Void> sendRequest(ISoftPullRequest body, Aggregator aggregator) {
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        return client.post().uri("/softpull")
                .contentType(MediaType.APPLICATION_JSON)
                .header("api-key", apiKey)
                .header("api-secret", apiSecret)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ISoftPullResponse.class)
                .flatMap(resp -> saveResult(resp, aggregator))
                .doOnError(err -> log.error("iSoftPull request failed", err))
                .then(updateAggregatorStatus(aggregator));
    }

    private Mono<CreditBureauResult> saveResult(ISoftPullResponse resp, Aggregator aggregator) {
        try {
            String json = mapper.writeValueAsString(resp);
            CreditBureauResult result = new CreditBureauResult(null, aggregator.getLoanId(), Json.of(json));
            return resultRepository.save(result);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> updateAggregatorStatus(Aggregator aggregator) {
        aggregator.setCreditBureauStatus(ServiceStatus.REQUESTED);
        return aggregatorRepository.save(aggregator).then();
    }
}
