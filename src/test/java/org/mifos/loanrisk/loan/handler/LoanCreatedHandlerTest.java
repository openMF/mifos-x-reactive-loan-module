package org.mifos.loanrisk.loan.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.domain.LoanSnapshot;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.mifos.loanrisk.service.AggregatorService;
import reactor.core.publisher.Mono;

class LoanCreatedHandlerTest {

    private AggregatorService service;
    private LoanSnapshotRepository snapshotRepo;
    private ObjectMapper mapper;
    private LoanCreatedHandler handler;
    private LoanAccountDataV1 loan;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(AggregatorService.class);
        snapshotRepo = mock(LoanSnapshotRepository.class);
        mapper = mock(ObjectMapper.class);
        loan = mock(LoanAccountDataV1.class);
        when(loan.getId()).thenReturn(1L);
        handler = new LoanCreatedHandler(service, snapshotRepo, mapper);
    }

    @Test
    void handleCreatesAggregatorAndSnapshot() throws Exception {
        JsonNode payload = JsonNodeFactory.instance.objectNode();
        when(mapper.treeToValue(payload, LoanAccountDataV1.class)).thenReturn(loan);
        when(mapper.writeValueAsString(payload)).thenReturn("{}");
        when(service.onLoanCreated(loan)).thenReturn(Mono.empty());
        when(snapshotRepo.existsById(1L)).thenReturn(Mono.just(false));
        when(snapshotRepo.save(any())).thenReturn(Mono.just(new LoanSnapshot()));

        handler.handle(payload);

        verify(service).onLoanCreated(loan);
        verify(snapshotRepo).existsById(1L);
        verify(snapshotRepo).save(any(LoanSnapshot.class));
    }
}
