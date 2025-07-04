package org.mifos.loanrisk.loan.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.common.EventCategory;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.mifos.loanrisk.loan.handler.LoanMessageHandler;

class LoanEventServiceTest {

    private LoanMessageHandler created;
    private LoanMessageHandler updated;
    private LoanMessageHandler withdrawn;
    private LoanMessageHandler rejected;
    private LoanEventService service;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        created = mock(LoanMessageHandler.class);
        updated = mock(LoanMessageHandler.class);
        withdrawn = mock(LoanMessageHandler.class);
        rejected = mock(LoanMessageHandler.class);

        doNothing().when(created).handle(any(JsonNode.class));
        doNothing().when(updated).handle(any(JsonNode.class));
        doNothing().when(withdrawn).handle(any(JsonNode.class));
        doNothing().when(rejected).handle(any(JsonNode.class));

        service = new LoanEventService(
                Map.of(LoanEventType.LoanCreatedBusinessEvent, created, LoanEventType.LoanApplicationModifiedBusinessEvent, updated,
                        LoanEventType.LoanWithdrawnByApplicantBusinessEvent, withdrawn, LoanEventType.LoanRejectedBusinessEvent, rejected));
    }

    @Test
    void dispatchesCreated() throws JsonProcessingException {
        JsonNode p = JsonNodeFactory.instance.objectNode();
        service.handle(env("LoanCreatedBusinessEvent", p));
        verify(created).handle(p);
        verifyNoInteractions(updated, withdrawn, rejected);
    }

    @Test
    void dispatchesUpdated() throws JsonProcessingException {
        JsonNode p = JsonNodeFactory.instance.objectNode();
        service.handle(env("LoanApplicationModifiedBusinessEvent", p));
        verify(updated).handle(p);
        verifyNoInteractions(created, withdrawn, rejected);
    }

    @Test
    void dispatchesWithdrawn() throws JsonProcessingException {
        JsonNode p = JsonNodeFactory.instance.objectNode();
        service.handle(env("LoanWithdrawnByApplicantBusinessEvent", p));
        verify(withdrawn).handle(p);
        verifyNoInteractions(created, updated, rejected);
    }

    @Test
    void dispatchesRejected() throws JsonProcessingException {
        JsonNode p = JsonNodeFactory.instance.objectNode();
        service.handle(env("LoanRejectedBusinessEvent", p));
        verify(rejected).handle(p);
        verifyNoInteractions(created, updated, withdrawn);
    }

    @Test
    void ignoresUnregistered() throws JsonProcessingException {
        JsonNode p = JsonNodeFactory.instance.objectNode();
        service.handle(env("SomeOtherEvent", p));
        verifyNoInteractions(created, updated, withdrawn, rejected);
    }

    private EventEnvelope env(String type, JsonNode payload) {
        return new EventEnvelope(1L, EventCategory.Loan, type, payload, "tenant", null, null);
    }
}
