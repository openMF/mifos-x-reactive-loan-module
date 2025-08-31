package org.mifos.loanrisk.external.cb.isoftpull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.mifos.loanrisk.domain.LoanSnapshot;
import org.mifos.loanrisk.fineract.FineractClientService;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Maps stored loan snapshot attributes into the iSoftPull request structure.
 */
@Component
@RequiredArgsConstructor
public class ISoftPullRequestMapper {

    private final LoanSnapshotRepository loanSnapshotRepository;
    private final FineractClientService fineractClientService;
    private final ObjectMapper mapper;

    public Mono<ISoftPullRequest> map(Long loanId) {
        return loanSnapshotRepository.findByLoanId(loanId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Loan snapshot not found for loan " + loanId)))
                .flatMap(this::fromSnapshot);
    }

    private Mono<ISoftPullRequest> fromSnapshot(LoanSnapshot snapshot) {
        try {
            JsonNode root = mapper.readTree(snapshot.getPayload());
            Long clientId = longValue(root, "clientId", "clientid");
            if (clientId == null) {
                return Mono.error(new IllegalStateException("Client ID not found in loan snapshot for loan " + snapshot.getLoanId()));
            }

            Mono<Entry<String, JsonNode>> detailsMono = fineractClientService.fetchClientDetails(clientId)
                    .map(j -> Map.entry("details", j));
            Mono<Entry<String, JsonNode>> addressMono = fineractClientService.fetchClientAddress(clientId)
                    .map(j -> {
                        JsonNode firstAddress = j.isArray() && j.size() > 0 ? j.get(0) : j;
                        return Map.entry("address", firstAddress);
                    });
            Mono<Entry<String, JsonNode>> identifiersMono = fineractClientService.fetchClientIdentifiers(clientId)
                    .map(j -> Map.entry("identifiers", j));

            return Flux.mergeDelayError(2, detailsMono, addressMono, identifiersMono)
                    .collectMap(Entry::getKey, Entry::getValue)
                    .flatMap(map -> {
                        JsonNode ids = map.get("identifiers");
                        JsonNode first = ids.isArray() && ids.size() > 0 ? ids.get(0) : null;
                        if (first == null || first.get("id") == null) {
                            return Mono.error(new IllegalStateException("No identifier found for client " + clientId));
                        }
                        Long identifierId = first.get("id").asLong();
                        return fineractClientService.fetchClientSsn(clientId, identifierId)
                                .map(ssn -> {
                                    JsonNode details = map.get("details");
                                    JsonNode address = map.get("address");

                                    String firstName = text(details, "firstName", "firstname");
                                    String lastName = text(details, "lastName", "lastname");
                                    String addr = text(address, "address", "addressLine1", "street");
                                    String city = text(address, "city", "town");
                                    String state = text(address, "state", "stateProvince", "stateName");
                                    String zip = text(address, "zip", "postalCode", "zipcode");
                                    String ssnNumber = text(ssn, "documentKey", "ssn", "ssnNumber");
                                    return new ISoftPullRequest(firstName, lastName, addr, city, state, zip, ssnNumber);
                                });
                    });
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to parse loan snapshot payload", e));
        }
    }

    private String text(JsonNode root, String... fields) {
        for (String f : fields) {
            JsonNode node = root.findValue(f);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                return node.asText();
            }
        }
        return "";
    }

    private Long longValue(JsonNode root, String... fields) {
        for (String f : fields) {
            JsonNode node = root.findValue(f);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                if (node.isNumber()) {
                    return node.asLong();
                }
                try {
                    return Long.parseLong(node.asText());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}

