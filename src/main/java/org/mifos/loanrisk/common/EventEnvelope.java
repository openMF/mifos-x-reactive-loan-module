package org.mifos.loanrisk.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record EventEnvelope(Long id, EventCategory category, String type, JsonNode payload, String tenantId, LocalDateTime createdAt,
        String businessDate) {
    /* convenience helpers */

    public boolean isLoan() {
        return category == EventCategory.LOAN;
    }

    public boolean isDocument() {
        return category == EventCategory.DOCUMENT;
    }

    public String getCategory() {
        return category.toString();
    }

    public String getType() {
        return type;
    }

    // get payload
    public JsonNode getPayload() {
        return payload;
    }

}
