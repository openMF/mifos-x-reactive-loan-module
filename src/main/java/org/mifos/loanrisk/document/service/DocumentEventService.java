package org.mifos.loanrisk.document.service;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.handler.DocumentMessageHandler;
import org.mifos.loanrisk.messaging.dispatcher.DomainEventService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentEventService implements DomainEventService {

    private final Map<DocumentEventType, DocumentMessageHandler> handlers;

    @Override
    public void handle(EventEnvelope env) throws JsonProcessingException {
        DocumentEventType type;
        try{
            type = DocumentEventType.valueOf(env.getType());
        } catch (IllegalArgumentException ex) {
            log.warn("Unsupported document event type: {}", env.getType());
            return;
        }
        handlers.get(type).handle(env.getPayload());
    }
}