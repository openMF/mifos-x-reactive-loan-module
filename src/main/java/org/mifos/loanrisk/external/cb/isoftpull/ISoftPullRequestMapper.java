package org.mifos.loanrisk.external.cb.isoftpull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mifos.loanrisk.domain.LoanSnapshot;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Maps stored loan snapshot attributes into the iSoftPull request structure.
 */
@Component
@RequiredArgsConstructor
public class ISoftPullRequestMapper {

    private final LoanSnapshotRepository loanSnapshotRepository;
    private final ObjectMapper mapper;

    public Mono<ISoftPullRequest> map(Long loanId) {
        return loanSnapshotRepository.findByLoanId(loanId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Loan snapshot not found for loan " + loanId)))
                .map(this::fromSnapshot);
    }

    private ISoftPullRequest fromSnapshot(LoanSnapshot snapshot) {
        try {
            JsonNode root = mapper.readTree(snapshot.getPayload());
            String firstName = text(root, "firstName", "firstname");
            String lastName = text(root, "lastName", "lastname");
            String address = text(root, "address", "addressLine1", "street");
            String city = text(root, "city", "town");
            String state = text(root, "state", "stateProvince");
            String zip = text(root, "zip", "postalCode", "zipcode");
            String ssn = text(root, "ssn", "ssnNumber");
            return new ISoftPullRequest(firstName, lastName, address, city, state, zip, ssn);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse loan snapshot payload", e);
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
}

