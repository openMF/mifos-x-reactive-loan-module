package org.mifos.loanrisk.document.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.handler.DocumentMessageHandler;
import org.mifos.loanrisk.messaging.dispatcher.DomainEventService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentEventService implements DomainEventService {

    private final Map<DocumentEventType, DocumentMessageHandler> handlers;

    @Override
    public void handle(EventEnvelope env) {
        DocumentEventType type = DocumentEventType.valueOf(env.getType());
        handlers.get(type).handle(env.getPayload());
    }
}
