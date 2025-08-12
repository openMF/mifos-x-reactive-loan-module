package org.mifos.loanrisk.external.bsa.arya;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Arya's bank statement analysis request body.
 */
public record AryaBsaRequest(
        @JsonProperty("doc_type") String docType,
        @JsonProperty("req_id") String reqId,
        @JsonProperty("report_type") String reportType,
        @JsonProperty("doc_base64") String docBase64) {
}

