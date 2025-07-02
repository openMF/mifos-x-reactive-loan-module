package org.mifos.loanrisk.loan.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.mifos.loanrisk.domain.LoanSnapshot;
import org.mifos.loanrisk.loan.common.Handles;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.mifos.loanrisk.service.AggregatorService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Handles(LoanEventType.LoanApplicationModifiedBusinessEvent)
@RequiredArgsConstructor
@Slf4j
public class LoanUpdatedHandler implements LoanMessageHandler {

    private final AggregatorService aggregatorService;
    private final LoanSnapshotRepository snapshotRepo;
    private final ObjectMapper mapper;

    @Override
    public void handle(JsonNode payload) throws JsonProcessingException {
        LoanAccountDataV1 dto = mapper.treeToValue(payload, LoanAccountDataV1.class);
        String jsonText = mapper.writeValueAsString(payload);
        Mono.when(updateAggregator(dto), updateExistingSnapshot(dto.getId(), jsonText))
                .doOnError(ex -> log.error("LoanUpdated flow failed for loan {}", dto.getId(), ex)).subscribe();
    }

    private Mono<Void> updateAggregator(LoanAccountDataV1 dto) {
        return aggregatorService.onLoanUpdated(dto);
    }

    private Mono<Void> updateExistingSnapshot(Long loanId, String json) {
        return snapshotRepo.findByLoanId(loanId).switchIfEmpty(Mono.error(new IllegalStateException("No snapshot row for loan " + loanId)))
                .flatMap(snap -> applyChanges(snap, json)).then();
    }

    private Mono<LoanSnapshot> applyChanges(LoanSnapshot snap, String json) {
        snap.setPayload(json);
        snap.setSnapshotAt(LocalDateTime.now());
        return snapshotRepo.save(snap).doOnSuccess(s -> log.info("Snapshot updated loan={}", s.getLoanId()));
    }
}
