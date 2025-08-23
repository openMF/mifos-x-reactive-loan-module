package org.mifos.loanrisk.fineract;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for fetching client related information from Fineract.
 */
@Service
@RequiredArgsConstructor
public class FineractClientService {

    private final WebClient fineractClient;

    /**
     * Fetch client details for the given client id.
     *
     * @param clientId the Fineract client identifier
     * @return client details as a JSON object
     */
    public Mono<JsonNode> fetchClientDetails(Long clientId) {
        return fineractClient.get()
                .uri("/clients/{clientId}", clientId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /**
     * Fetch the client's SSN/identifier information.
     *
     * @param clientId the Fineract client identifier
     * @return identifier details as a JSON object
     */
    public Mono<JsonNode> fetchClientSsn(Long clientId) {
        return fineractClient.get()
                .uri("/clients/{clientId}/identifiers/1", clientId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /**
     * Fetch the client's address details.
     *
     * @param clientId the Fineract client identifier
     * @return address details as a JSON object
     */
    public Mono<JsonNode> fetchClientAddress(Long clientId) {
        return fineractClient.get()
                .uri("/client/{clientId}/addresses?type=804", clientId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}

