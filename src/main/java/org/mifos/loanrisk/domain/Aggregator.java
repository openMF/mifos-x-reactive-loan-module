package org.mifos.loanrisk.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.mifos.loanrisk.common.LoanStatus;
import org.mifos.loanrisk.common.ServiceStatus;
import org.mifos.loanrisk.document.common.DocumentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("aggregator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Aggregator {

    @Id
    private Long id; // SERIAL / identity

    @NonNull
    @Column("loan_id")
    private Long loanId;

    @Column("tenant_id")
    private String tenantId;

    /* loan & document flags */
    @Column("loan_status")
    private LoanStatus loanStatus; // enum stored as text

    @Column("bank_stmt_uploaded")
    private Boolean bankStmtUploaded;

    @Column("id_doc_uploaded")
    private Boolean idDocUploaded;

    @Column("kyc_doc_uploaded")
    private Boolean kycDocUploaded;

    /* external-service statuses */
    @Column("credit_bureau_status")
    private ServiceStatus creditBureauStatus;

    @Column("bank_stmt_status")
    private ServiceStatus bankStmtStatus;

    @Column("income_stmt_status")
    private ServiceStatus incomeStmtStatus;

    @Column("ml_score_status")
    private ServiceStatus mlScoreStatus;

    /* scores */
    @Column("credit_bureau_score")
    private BigDecimal creditBureauScore;

    @Column("bank_stmt_score")
    private BigDecimal bankStmtScore;

    @Column("income_stmt_score")
    private BigDecimal incomeStmtScore;

    @Column("ml_score")
    private BigDecimal mlScore;

    /* aggregated result */
    @Column("overall_score")
    private BigDecimal overallScore;

    @Column("risk_grade")
    private String riskGrade;

    @Column("assessment_status")
    private ServiceStatus assessmentStatus;

    @Column("last_updated")
    private LocalDateTime lastUpdated;

    /** Convenience constructor that omits the auto-generated ID. */
    public Aggregator(@NonNull Long loanId, String tenantId, LoanStatus loanStatus, Boolean bankStmtUploaded, Boolean idDocUploaded,
            Boolean kycDocUploaded, ServiceStatus creditBureauStatus, ServiceStatus bankStmtStatus, ServiceStatus incomeStmtStatus,
            ServiceStatus mlScoreStatus, BigDecimal creditBureauScore, BigDecimal bankStmtScore, BigDecimal incomeStmtScore,
            BigDecimal mlScore, BigDecimal overallScore, String riskGrade, ServiceStatus assessmentStatus, LocalDateTime lastUpdated) {
        this.loanId = loanId;
        this.tenantId = tenantId;
        this.loanStatus = loanStatus;
        this.bankStmtUploaded = bankStmtUploaded;
        this.idDocUploaded = idDocUploaded;
        this.kycDocUploaded = kycDocUploaded;
        this.creditBureauStatus = creditBureauStatus;
        this.bankStmtStatus = bankStmtStatus;
        this.incomeStmtStatus = incomeStmtStatus;
        this.mlScoreStatus = mlScoreStatus;
        this.creditBureauScore = creditBureauScore;
        this.bankStmtScore = bankStmtScore;
        this.incomeStmtScore = incomeStmtScore;
        this.mlScore = mlScore;
        this.overallScore = overallScore;
        this.riskGrade = riskGrade;
        this.assessmentStatus = assessmentStatus;
        this.lastUpdated = lastUpdated;
    }

    public void LoanCreated(@NonNull LoanAccountDataV1 loan, String tenantId) {
        this.loanId = loan.getId();
        this.tenantId = tenantId;
        this.loanStatus = LoanStatus.valueOf(loan.getStatus().getValue());
        this.bankStmtUploaded = false;
        this.idDocUploaded = false;
        this.kycDocUploaded = false;
        this.creditBureauStatus = ServiceStatus.PENDING;
        this.bankStmtStatus = ServiceStatus.PENDING;
        this.incomeStmtStatus = ServiceStatus.PENDING;
        this.mlScoreStatus = ServiceStatus.PENDING;
        this.creditBureauScore = null;
        this.bankStmtScore = null;
        this.incomeStmtScore = null;
        this.mlScore = null;
        this.overallScore = null;
        this.riskGrade = "UNKNOWN";
        this.assessmentStatus = ServiceStatus.PENDING;
        this.lastUpdated = LocalDateTime.now();
    }

    public void cancel() {
        this.assessmentStatus = ServiceStatus.CANCELLED;
        this.lastUpdated = LocalDateTime.now();
    }

    public void documentArrived(DocumentType dt) {
        setFlag(dt, true);
        reevaluateStatus();
    }

    public void documentDeleted(DocumentType dt) {
        setFlag(dt, false);
        reevaluateStatus();
    }

    private void setFlag(DocumentType dt, boolean present) {
        switch (dt) {
            case BANK_STATEMENT -> bankStmtUploaded = present;
            case ID_DOC -> idDocUploaded = present;
            case KYC_DOC -> kycDocUploaded = present;
        }
    }

    /** recompute PENDING status */
    private void reevaluateStatus() {
        // implement the core logic to determine if the assessment is ready here
        // TODO: this is a placeholder, actual logic not yet implemented
        if (this.assessmentStatus == ServiceStatus.CANCELLED) return; // terminal
        boolean ready = Boolean.TRUE.equals(bankStmtUploaded) && Boolean.TRUE.equals(idDocUploaded) && Boolean.TRUE.equals(kycDocUploaded);

        this.assessmentStatus = ready ? ServiceStatus.REQUESTED : ServiceStatus.PENDING;
    }

}
