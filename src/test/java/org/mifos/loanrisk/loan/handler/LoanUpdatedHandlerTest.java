package org.mifos.loanrisk.loan.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.LocalDateTime;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.domain.LoanSnapshot;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.mifos.loanrisk.service.AggregatorService;
import reactor.core.publisher.Mono;

class LoanUpdatedHandlerTest {

    private AggregatorService service;
    private LoanSnapshotRepository snapshotRepo;
    private ObjectMapper mapper;
    private LoanUpdatedHandler handler;
    private LoanAccountDataV1 loan;
    private LoanSnapshot snapshot;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(AggregatorService.class);
        snapshotRepo = mock(LoanSnapshotRepository.class);
        mapper = mock(ObjectMapper.class);
        loan = mock(LoanAccountDataV1.class);
        when(loan.getId()).thenReturn(1L);
        snapshot = new LoanSnapshot(1L, "old", LocalDateTime.now().minusDays(1));
        handler = new LoanUpdatedHandler(service, snapshotRepo, mapper);
    }

    @Test
    void handleUpdatesAggregatorAndSnapshot() throws Exception {
        JsonNode payload = JsonNodeFactory.instance.objectNode();
        when(mapper.treeToValue(payload, LoanAccountDataV1.class)).thenReturn(loan);
        when(mapper.writeValueAsString(payload)).thenReturn("new-json");
        when(service.onLoanUpdated(loan)).thenReturn(Mono.empty());
        when(snapshotRepo.findByLoanId(1L)).thenReturn(Mono.just(snapshot));
        when(snapshotRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        handler.handle(payload);

        verify(service).onLoanUpdated(loan);
        verify(snapshotRepo).findByLoanId(1L);
        verify(snapshotRepo).save(snapshot);
        assertEquals("new-json", snapshot.getPayload());
    }
}
