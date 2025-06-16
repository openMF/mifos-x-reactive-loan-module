package org.mifos.loanrisk.event;

import lombok.Getter;

@Getter
public class EventMessageDTO {
    private final Long eventId;
    private final String type;
    private final String category;
    private final String tenantId;
    private final String createdAt;
    private final String payLoad;
    private final String businessDate;

    public EventMessageDTO(Long eventId, String type, String category, String tenantId, String createdAt, String payLoad, String businessDate) {
        this.eventId = eventId;
        this.type = type;
        this.category = category;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
        this.payLoad = payLoad;
        this.businessDate = businessDate;
    }

    @Override
    public String toString() {
        return "EventMessageDTO{" +
            "eventId=" + eventId +
            ", type='" + type + '\'' +
            ", category='" + category + '\'' +
            ", tenantId='" + tenantId + '\'' +
            ", createdAt='" + createdAt + '\'' +
            ", payLoad='" + payLoad + '\'' +
            ", businessDate='" + businessDate + '\'' +
            '}';
    }
}
