package org.mifos.loanrisk.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("event_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {

    @Id
    private Long id;               // SERIAL / identity column

    @Column("event_id")
    private Long eventId;

    @Column("type")
    private String type;

    @Column("category")
    private String category;

    @Column("data_schema")
    private String schema;

    @Column("tenant_id")
    private String tenantId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("payload")
    private byte[] payload;

    @Column("business_date")
    private String businessDate;

    /** Convenience constructor that omits the auto-generated ID. */
    public EventMessage(Long eventId,
                        String type,
                        String category,
                        String schema,
                        String tenantId,
                        LocalDateTime createdAt,
                        byte[] payload,
                        String businessDate) {

        this.eventId       = eventId;
        this.type          = type;
        this.category      = category;
        this.schema        = schema;
        this.tenantId      = tenantId;
        this.createdAt     = createdAt;
        this.payload       = payload;
        this.businessDate  = businessDate;
    }
}
