package org.mifos.loanrisk.messaging.dispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mifos.loanrisk.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DomainEventService {

    Logger LOG = LoggerFactory.getLogger(DomainEventService.class);

    void handle(EventEnvelope env) throws JsonProcessingException;

    DomainEventService NOOP = env -> LOG.warn("No domain service for {}", env.getCategory());
}
