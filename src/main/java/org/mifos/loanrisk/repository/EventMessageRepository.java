package org.mifos.loanrisk.repository;

import org.mifos.loanrisk.domain.EventMessage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface EventMessageRepository extends R2dbcRepository<EventMessage, Long> {
    Flux<EventMessage> findByType(String eventType);
}
