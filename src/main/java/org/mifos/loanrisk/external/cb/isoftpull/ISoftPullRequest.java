package org.mifos.loanrisk.external.cb.isoftpull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for iSoftPull credit bureau request body.
 */
public record ISoftPullRequest(
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        @JsonProperty("address") String address,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("zip") String zip,
        @JsonProperty("ssn") String ssn) {
}
