package org.mifos.loanrisk.messaging.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.MessageV1;
import org.mifos.loanrisk.common.EventEnvelope;
import org.mifos.loanrisk.common.EventMapper;
import org.mifos.loanrisk.messaging.dispatcher.DomainEventDispatcher;
import org.mifos.loanrisk.messaging.domain.EventMessage;
import org.mifos.loanrisk.messaging.repository.EventMessageRepository;
import org.mifos.loanrisk.utility.ByteBufferConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageConsumerHandler implements MessageHandler {

    @Autowired
    private ByteBufferConvertor byteBufferConvertor;

    @Autowired
    private EventMessageRepository repository;

    @Autowired
    private TransactionalOperator txOperator;

    private final EventMapper eventMapper;

    private final DomainEventDispatcher dispatcher;

    @Override
    @KafkaListener(topics = "${app.kafka.topic:external-events}", containerFactory = "kafkaListenerContainerFactory")
    public void handleMessage(Message<?> springMessage) throws MessagingException {
        byte[] rawPayload = (byte[]) springMessage.getPayload();
        ByteBuffer wrapperBuf = byteBufferConvertor.convert(rawPayload);

        try {
            MessageV1 messagePayload = MessageV1.fromByteBuffer(wrapperBuf);
            log.info("Received Kafka event of Category = {}, Type = {}", messagePayload.getCategory(), messagePayload.getType());
            saveAndProcess(messagePayload).as(txOperator::transactional).subscribe();

        } catch (IOException ex) {
            log.error("Unable to process Kafka message", ex);
        }
    }

    private Mono<Void> saveAndProcess(MessageV1 messagePayload) {
        EventMessage message = eventMapper.toEntity(messagePayload);
        return repository.findByEventId(message.getEventId()).switchIfEmpty(repository.save(message))
                .flatMap(evt -> processEvent(evt, messagePayload));
    }

    private Mono<Void> processEvent(EventMessage evt, MessageV1 payload) {
        try {
            EventEnvelope env = eventMapper.toEnvelope(payload);
            dispatcher.dispatch(env);
            evt.setProcessed(true);
            return repository.save(evt).then();
        } catch (Exception ex) {
            log.error("Unable to process Kafka message", ex);
            evt.setProcessed(false);
            return repository.save(evt).then();
        }
    }

}
