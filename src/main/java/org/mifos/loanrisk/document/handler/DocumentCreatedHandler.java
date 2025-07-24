package org.mifos.loanrisk.document.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.common.Handles;
import org.mifos.loanrisk.document.service.fetch.DocumentFetchService;
import org.mifos.loanrisk.service.AggregatorService;
import org.springframework.stereotype.Component;

@Component
@Handles(DocumentEventType.DocumentCreatedBusinessEvent)
@RequiredArgsConstructor
@Slf4j
public class DocumentCreatedHandler implements DocumentMessageHandler {

    private final AggregatorService aggregatorService;
    private final DocumentFetchService documentFetchService;
    private final ObjectMapper mapper;

    @Override
    public void handle(JsonNode payload) throws JsonProcessingException {
        DocumentDataV1 documentData = mapper.treeToValue(payload, DocumentDataV1.class);
        aggregatorService
                .onDocumentCreated(documentData).then(documentFetchService.fetch(documentData.getParentEntityType(),
                        documentData.getParentEntityId(), documentData.getId()))
                .doOnError(ex -> log.error("DocumentCreated flow failed", ex)).subscribe();

    }
}
