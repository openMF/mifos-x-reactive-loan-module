package org.mifos.loanrisk.external.bsa.arya;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO for Arya's bank statement analysis response.
 */
public record AryaBsaResponse(
        @JsonProperty("req_id") String reqId,
        @JsonProperty("success") Boolean success,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("data") Map<String, Object> data) {
}

