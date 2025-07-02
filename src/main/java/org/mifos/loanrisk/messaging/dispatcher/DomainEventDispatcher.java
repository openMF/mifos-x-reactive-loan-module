package org.mifos.loanrisk.messaging.dispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.mifos.loanrisk.common.EventCategory;
import org.mifos.loanrisk.common.EventEnvelope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventDispatcher {

    private final Map<EventCategory, DomainEventService> services;

    public void dispatch(EventEnvelope env) throws JsonProcessingException {
        services.getOrDefault(EventCategory.valueOf(env.getCategory()), DomainEventService.NOOP).handle(env);
    }
}
