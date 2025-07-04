package org.mifos.loanrisk.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.common.LoanStatus;
import org.mifos.loanrisk.common.ServiceStatus;
import org.mifos.loanrisk.document.common.DocumentType;

class AggregatorTest {

    private LoanAccountDataV1 loan;
    private Aggregator aggregator;

    @BeforeEach
    void setUp() {
        loan = mock(LoanAccountDataV1.class, RETURNS_DEEP_STUBS);
        when(loan.getId()).thenReturn(1L);
        when(loan.getClientExternalId()).thenReturn("TENANT");
        when(loan.getStatus().getId()).thenReturn(LoanStatus.APPROVED.getValue());
        aggregator = new Aggregator(loan);
    }

    @Test
    void documentFlagsSetAndRequestedWhenAllPresent() {
        aggregator.documentArrived(DocumentType.BANK_STATEMENT);
        aggregator.documentArrived(DocumentType.ID_DOC);
        aggregator.documentArrived(DocumentType.KYC_DOC);
        assertTrue(aggregator.getBankStmtUploaded());
        assertTrue(aggregator.getIdDocUploaded());
        assertTrue(aggregator.getKycDocUploaded());
        assertEquals(ServiceStatus.REQUESTED, aggregator.getAssessmentStatus());
    }

    @Test
    void cancelLoanSetsCancelledStatus() {
        aggregator.cancelLoan();
        assertEquals(ServiceStatus.CANCELLED, aggregator.getAssessmentStatus());
    }

    @Test
    void updateFromLoanChangesTenantId() {
        when(loan.getClientExternalId()).thenReturn("NEW");
        aggregator.updateFromLoan(loan);
        assertEquals("NEW", aggregator.getTenantId());
        assertEquals(ServiceStatus.PENDING, aggregator.getAssessmentStatus());
    }
}
