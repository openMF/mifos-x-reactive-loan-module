package org.mifos.loanrisk.document.service;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.handler.DocumentMessageHandler;

class DocumentEventServiceTest {

    private DocumentMessageHandler createdHandler;
    private DocumentMessageHandler deletedHandler;
    private DocumentEventService service;

    @BeforeEach
    void setUp() {
        createdHandler = mock(DocumentMessageHandler.class);
        deletedHandler = mock(DocumentMessageHandler.class);
        service = new DocumentEventService(Map.of(DocumentEventType.DocumentCreatedBusinessEvent, createdHandler,
                DocumentEventType.DocumentDeletedBusinessEvent, deletedHandler));
    }

    @Test
    void handleDispatchesToCreatedHandler() throws JsonProcessingException {
        JsonNode payload = JsonNodeFactory.instance.objectNode();
        EventEnvelope env = new EventEnvelope(1L, null, "DocumentCreatedBusinessEvent", payload, null, null, null);
        service.handle(env);
        verify(createdHandler).handle(payload);
        verifyNoInteractions(deletedHandler);
    }

    @Test
    void handleDispatchesToDeletedHandler() throws JsonProcessingException {
        JsonNode payload = JsonNodeFactory.instance.objectNode();
        EventEnvelope env = new EventEnvelope(1L, null, "DocumentDeletedBusinessEvent", payload, null, null, null);
        service.handle(env);
        verify(deletedHandler).handle(payload);
        verifyNoInteractions(createdHandler);
    }
}
