package org.mifos.loanrisk.loan.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.mifos.loanrisk.loan.handler.LoanMessageHandler;
import org.mifos.loanrisk.messaging.dispatcher.DomainEventService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventService implements DomainEventService {

    private final Map<LoanEventType, LoanMessageHandler> handlers;

    @Override
    public void handle(EventEnvelope env) {
        LoanEventType type = LoanEventType.valueOf(env.getType());
        handlers.get(type).handle(env.getPayload());
    }
}
