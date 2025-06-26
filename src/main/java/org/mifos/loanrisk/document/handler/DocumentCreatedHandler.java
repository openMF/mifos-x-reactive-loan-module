package org.mifos.loanrisk.document.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.common.Handles;
import org.springframework.stereotype.Component;

@Component
@Handles(DocumentEventType.CREATED)
public class DocumentCreatedHandler implements DocumentMessageHandler {

    @Override
    public void handle(JsonNode raw) {

    }
}
