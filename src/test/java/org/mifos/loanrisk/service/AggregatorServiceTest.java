package org.mifos.loanrisk.service;

import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.common.ServiceStatus;
import org.mifos.loanrisk.document.common.DocumentType;
import org.mifos.loanrisk.domain.Aggregator;
import org.mifos.loanrisk.repository.AggregatorRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AggregatorServiceTest {

    private AggregatorRepository repo;
    private AggregatorService service;
    private LoanAccountDataV1 loan;

    @BeforeEach
    void setUp() {
        repo = mock(AggregatorRepository.class);
        service = new AggregatorService(repo);
        loan = mock(LoanAccountDataV1.class, RETURNS_DEEP_STUBS);
        when(loan.getId()).thenReturn(1L);
        when(loan.getClientExternalId()).thenReturn("T1");
        when(loan.getStatus().getId()).thenReturn(100);
    }

    @Test
    void onLoanCreatedSavesWhenAbsent() {
        when(repo.existsByLoanId(1L)).thenReturn(Mono.just(false));
        when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.onLoanCreated(loan))
                .assertNext(ag -> {
                    assertEquals(1L, ag.getLoanId());
                    assertEquals(ServiceStatus.PENDING, ag.getAssessmentStatus());
                })
                .verifyComplete();

        verify(repo).save(any(Aggregator.class));
    }

    @Test
    void onLoanCreatedSkipsWhenExists() {
        when(repo.existsByLoanId(1L)).thenReturn(Mono.just(true));
        StepVerifier.create(service.onLoanCreated(loan)).verifyComplete();
        verify(repo, never()).save(any());
    }

    @Test
    void onLoanUpdatedPersistsChanges() {
        Aggregator ag = new Aggregator(loan);
        when(repo.findByLoanId(1L)).thenReturn(Mono.just(ag));
        when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.onLoanUpdated(loan)).verifyComplete();

        verify(repo).save(ag);
        assertEquals(ServiceStatus.PENDING, ag.getAssessmentStatus());
    }

    @Test
    void onLoanWithdrawnCancelsAggregator() {
        Aggregator ag = new Aggregator(loan);
        when(repo.findByLoanId(1L)).thenReturn(Mono.just(ag));
        when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.onLoanWithdrawn(loan)).verifyComplete();

        assertEquals(ServiceStatus.CANCELLED, ag.getAssessmentStatus());
        verify(repo).save(ag);
    }

    @Test
    void onDocumentCreatedUpdatesAggregator() {
        Aggregator ag = new Aggregator(loan);
        DocumentDataV1 doc = mock(DocumentDataV1.class, RETURNS_DEEP_STUBS);
        when(doc.getParentEntityType()).thenReturn("loan");
        when(doc.getParentEntityId()).thenReturn(1L);
        when(doc.getName()).thenReturn("bankStatement");
        when(repo.findByLoanId(1L)).thenReturn(Mono.just(ag));
        when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.onDocumentCreated(doc)).verifyComplete();

        assertTrue(ag.getBankStmtUploaded());
        verify(repo).save(ag);
    }
}
