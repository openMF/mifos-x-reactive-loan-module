package org.mifos.loanrisk.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("loan_snapshot")
@Data
@NoArgsConstructor
public class LoanSnapshot {

    @Id
    private Long loanId; // loan_id = LoanAccountDataV1.getId()

    private String accountNo;
    private Long clientId;
    private BigDecimal principal;
    private BigDecimal annualInterestRate;
    private Integer termFrequency;
    private String status; // enum as text (“SUBMITTED”, “APPROVED”, …)

    @Column("raw_payload") // full Avro payload
    private byte[] rawPayload;

    @Column("snapshot_at")
    private LocalDateTime snapshotAt;
}
