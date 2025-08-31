package org.mifos.loanrisk.external.cb.isoftpull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO for iSoftPull credit bureau response.
 */
public record ISoftPullResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("data") Map<String, Object> data) {
}
