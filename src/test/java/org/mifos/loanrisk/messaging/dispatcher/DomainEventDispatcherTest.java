package org.mifos.loanrisk.messaging.dispatcher;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.common.EventCategory;
import org.mifos.loanrisk.common.EventEnvelope;

class DomainEventDispatcherTest {

    private DomainEventService loanService;
    private DomainEventService docService;
    private DomainEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        loanService = mock(DomainEventService.class);
        docService = mock(DomainEventService.class);
        dispatcher = new DomainEventDispatcher(Map.of(EventCategory.Loan, loanService, EventCategory.DOCUMENT, docService));
    }

    @Test
    void dispatchRoutesToCorrectService() throws Exception {
        EventEnvelope env = new EventEnvelope(1L, EventCategory.Loan, "TYPE", JsonNodeFactory.instance.objectNode(), null, null, null);
        dispatcher.dispatch(env);
        verify(loanService).handle(env);
        verifyNoInteractions(docService);
    }
}
