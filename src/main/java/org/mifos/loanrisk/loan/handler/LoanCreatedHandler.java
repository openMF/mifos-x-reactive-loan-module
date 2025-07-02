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
@Handles(LoanEventType.LoanCreatedBusinessEvent)
@RequiredArgsConstructor
@Slf4j
public class LoanCreatedHandler implements LoanMessageHandler {

    private final AggregatorService service;
    private final LoanSnapshotRepository snapshotRepo;
    private final ObjectMapper mapper;

    @Override
    public void handle(JsonNode payload) throws JsonProcessingException {
        LoanAccountDataV1 dto = mapper.treeToValue(payload, LoanAccountDataV1.class);
        LoanSnapshot snap = snapshot(dto, payload);
        Mono.when(service.onLoanCreated(dto), saveSnapshotIfAbsent(dto.getId(), snap))
                .doOnError(ex -> log.error("LoanCreated flow failed", ex)).subscribe();
    }

    private Mono<Void> saveSnapshotIfAbsent(Long loanId, LoanSnapshot snap) {
        return snapshotRepo.existsById(loanId)
                .flatMap(exists -> exists ? Mono.fromRunnable(() -> log.warn("Snapshot already exists for loan {}", loanId))
                        : snapshotRepo.save(snap).doOnSuccess(s -> log.info("Snapshot saved {}", s.getLoanId())))
                .then();
    }

    private LoanSnapshot snapshot(LoanAccountDataV1 src, JsonNode payload) throws JsonProcessingException {
        return new LoanSnapshot(src.getId(), mapper.writeValueAsString(payload), LocalDateTime.now());
    }
}
