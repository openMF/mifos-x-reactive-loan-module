package org.mifos.loanrisk.messaging.repository;

import org.mifos.loanrisk.messaging.domain.EventMessage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventMessageRepository extends R2dbcRepository<EventMessage, Long> {

    Flux<EventMessage> findByType(String eventType);

    Mono<Boolean> existsByEventId(Long eventId);

    Mono<EventMessage> findByEventId(Long eventId);

    Flux<EventMessage> findByProcessedFalse();
}
