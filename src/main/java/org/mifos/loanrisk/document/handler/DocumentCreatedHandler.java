package org.mifos.loanrisk.document.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.common.Handles;
import org.mifos.loanrisk.common.LoanStatus;
import org.mifos.loanrisk.common.ServiceStatus;
import org.mifos.loanrisk.service.AggregatorService;
import org.mifos.loanrisk.external.bsa.BankStatementAnalysisService;
import org.mifos.loanrisk.external.bsa.BankStatementAnalysisServiceFactory;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Component;

@Component
@Handles(DocumentEventType.DocumentCreatedBusinessEvent)
@RequiredArgsConstructor
@Slf4j
public class DocumentCreatedHandler implements DocumentMessageHandler {

    private final AggregatorService aggregatorService;
    private final BankStatementAnalysisServiceFactory bsaFactory;
    private final ObjectMapper mapper;

    @Override
    public void handle(JsonNode payload) throws JsonProcessingException {
        DocumentDataV1 documentData = mapper.treeToValue(payload, DocumentDataV1.class);
        aggregatorService.onDocumentCreated(documentData)
                .then(initiateExternalServices(documentData))
                .doOnError(ex -> log.error("DocumentCreated flow failed", ex)).subscribe();

    }

    /**
     * After the generic document processing is complete, determine which
     * external services can now be triggered and initiate them.
     */
    private Mono<Void> initiateExternalServices(DocumentDataV1 documentData) {
        if (!"loans".equalsIgnoreCase(documentData.getParentEntityType())) {
            return Mono.empty();
        }

        return aggregatorService.getByLoanId(documentData.getParentEntityId())
                .flatMap(ag -> {
                    // Bank statement analysis requirements
                    if (ag.getLoanStatus() == LoanStatus.SUBMITTED_AND_PENDING_APPROVAL
                            && Boolean.TRUE.equals(ag.getBankStmtUploaded())
                            && ag.getBankStmtStatus() == ServiceStatus.PENDING) {
                        BankStatementAnalysisService svc = bsaFactory.getService();
                        return svc.analyze(documentData, ag);
                    }
                    return Mono.empty();
                });
    }
}
