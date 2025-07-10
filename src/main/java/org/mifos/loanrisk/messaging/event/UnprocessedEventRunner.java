package org.mifos.loanrisk.messaging.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.loanrisk.common.EventMapper;
import org.mifos.loanrisk.messaging.dispatcher.DomainEventDispatcher;
import org.mifos.loanrisk.messaging.domain.EventMessage;
import org.mifos.loanrisk.messaging.repository.EventMessageRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnprocessedEventRunner implements ApplicationRunner {

    private final EventMessageRepository repository;
    private final EventMapper mapper;
    private final DomainEventDispatcher dispatcher;

    @Override
    public void run(ApplicationArguments args) {
        repository.findByProcessedFalse().flatMap(this::process).onErrorContinue((ex, obj) -> log.error("Failed to reprocess event", ex))
                .subscribe();
    }

    private Flux<EventMessage> process(EventMessage msg) {
        try {
            dispatcher.dispatch(mapper.toEnvelope(msg));
            msg.setProcessed(true);
            log.info("Successfully processed unprocessed event {}", msg.getEventId());
        } catch (Exception e) {
            log.error("Failed processing event {}", msg.getEventId(), e);
            msg.setProcessed(false);
        }
        return repository.save(msg).flux();
    }
}
