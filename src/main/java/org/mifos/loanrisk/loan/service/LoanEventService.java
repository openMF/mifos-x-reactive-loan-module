package org.mifos.loanrisk.loan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.mifos.loanrisk.loan.handler.LoanMessageHandler;
import org.mifos.loanrisk.messaging.dispatcher.DomainEventService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventService implements DomainEventService {

    private final Map<LoanEventType, LoanMessageHandler> handlers;

    @Override
    public void handle(EventEnvelope env) throws JsonProcessingException {
        LoanEventType type;
        try {
            type = LoanEventType.valueOf(env.getType());
        } catch (IllegalArgumentException ex) {
            log.warn("Unsupported loan event type: {}", env.getType());
            return;
        }
        handlers.get(type).handle(env.getPayload());
    }
}
