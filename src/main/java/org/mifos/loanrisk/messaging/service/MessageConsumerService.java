package org.mifos.loanrisk.messaging.service;

import org.mifos.loanrisk.messaging.event.EventMessageDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageConsumerService {

    Flux<EventMessageDTO> getMessages();

    Flux<EventMessageDTO> getMessagesByType(String eventType);

    Mono<Void> deleteMessages();
}
