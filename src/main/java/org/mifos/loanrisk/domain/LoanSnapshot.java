package org.mifos.loanrisk.domain;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("loan_snapshot")
@Data
@NoArgsConstructor
public class LoanSnapshot {

    @Id
    private Long id;

    @NonNull
    @Column("loan_id")
    private Long loanId; // loan_id = LoanAccountDataV1.getId()

    @Column("payload") // full Avro payload
    private String payload;

    @Column("snapshot_at")
    private LocalDateTime snapshotAt;

    public LoanSnapshot(Long loanId, String payload, LocalDateTime snapshotAt) {
        this.loanId = loanId;
        this.payload = payload;
        this.snapshotAt = snapshotAt;
    }
}
