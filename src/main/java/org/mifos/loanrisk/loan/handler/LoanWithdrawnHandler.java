package org.mifos.loanrisk.loan.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.mifos.loanrisk.loan.common.Handles;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.mifos.loanrisk.repository.LoanSnapshotRepository;
import org.mifos.loanrisk.service.AggregatorService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Handles(LoanEventType.LoanWithdrawnByApplicantBusinessEvent)
@RequiredArgsConstructor
@Slf4j
public class LoanWithdrawnHandler implements LoanMessageHandler {

    private final AggregatorService aggregatorService;
    private final LoanSnapshotRepository snapshotRepo;
    private final ObjectMapper mapper;

    @Override
    public void handle(JsonNode payload) throws JsonProcessingException {
        LoanAccountDataV1 dto = mapper.treeToValue(payload, LoanAccountDataV1.class);
        Mono.when(aggregatorService.onLoanWithdrawn(dto),
                snapshotRepo.deleteByLoanId(dto.getId()).doOnSuccess(v -> log.info("Snapshot deleted loan={}", dto.getId())))
                .doOnError(ex -> log.error("LoanWithdrawn flow failed for loan {}", dto.getId(), ex)).subscribe();
    }
}
