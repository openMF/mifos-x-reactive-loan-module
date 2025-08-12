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

    @Column("bank_stmt_id")
    private Long bankStmtId;

    @Column("id_doc_uploaded")
    private Boolean idDocUploaded;

    @Column("id_doc_id")
    private Long idDocId;

    @Column("kyc_doc_uploaded")
    private Boolean kycDocUploaded;

    @Column("kyc_doc_id")
    private Long kycDocId;

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
    public Aggregator(@NonNull Long loanId, String tenantId, LoanStatus loanStatus, Boolean bankStmtUploaded, Long bankStmtId,
            Boolean idDocUploaded, Long idDocId, Boolean kycDocUploaded, Long kycDocId, ServiceStatus creditBureauStatus,
            ServiceStatus bankStmtStatus, ServiceStatus incomeStmtStatus, ServiceStatus mlScoreStatus, BigDecimal creditBureauScore,
            BigDecimal bankStmtScore, BigDecimal incomeStmtScore, BigDecimal mlScore, BigDecimal overallScore, String riskGrade,
            ServiceStatus assessmentStatus, LocalDateTime lastUpdated) {
        this.loanId = loanId;
        this.tenantId = tenantId;
        this.loanStatus = loanStatus;
        this.bankStmtUploaded = bankStmtUploaded;
        this.bankStmtId = bankStmtId;
        this.idDocUploaded = idDocUploaded;
        this.idDocId = idDocId;
        this.kycDocUploaded = kycDocUploaded;
        this.kycDocId = kycDocId;
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

    public Aggregator(@NonNull LoanAccountDataV1 loan) {
        this.loanId = loan.getId();
        this.tenantId = loan.getClientExternalId();
        this.loanStatus = LoanStatus.fromInt(loan.getStatus().getId());
        this.bankStmtUploaded = false;
        this.bankStmtId = null;
        this.idDocUploaded = false;
        this.idDocId = null;
        this.kycDocUploaded = false;
        this.kycDocId = null;
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

    public void cancelLoan() {
        this.assessmentStatus = ServiceStatus.CANCELLED;
        this.lastUpdated = LocalDateTime.now();
        // handleLoanCancellation();
        // have to implement this method to handle stopping all external services
        // and setting all statuses to CANCELLED
    }

    public void updateFromLoan(@NonNull LoanAccountDataV1 loan) {
        this.loanId = loan.getId();
        this.tenantId = loan.getClientExternalId();
        this.loanStatus = LoanStatus.fromInt(loan.getStatus().getId());
        this.lastUpdated = LocalDateTime.now();
    }

    public void documentArrived(DocumentType dt, Long documentId) {
        setFlag(dt, true, documentId);
        // reevaluateStatus();
    }

    public void documentDeleted(DocumentType dt) {
        setFlag(dt, false, null);
        // reevaluateStatus();
    }

    private void setFlag(DocumentType dt, boolean present, Long documentId) {
        switch (dt) {
            case BANK_STATEMENT -> {
                bankStmtUploaded = present;
                bankStmtId = present ? documentId : null;
            }
            case ID_DOC -> {
                idDocUploaded = present;
                idDocId = present ? documentId : null;
            }
            case KYC_DOC -> {
                kycDocUploaded = present;
                kycDocId = present ? documentId : null;
            }
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
