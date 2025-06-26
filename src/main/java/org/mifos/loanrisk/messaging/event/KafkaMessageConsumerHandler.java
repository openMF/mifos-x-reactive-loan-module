package org.mifos.loanrisk.messaging.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("kafka")
public class KafkaMessageConsumerHandler implements MessageHandler {

    @Autowired
    private ByteBufferConvertor byteBufferConvertor;

    @Autowired
    private EventMessageRepository repository;

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
            saveMessage(messagePayload);
            EventEnvelope env = eventMapper.toEnvelope(messagePayload);
            dispatcher.dispatch(env);

        } catch (IOException | ReflectiveOperationException ex) {
            log.error("Unable to process Kafka message", ex);
        }
    }

    private void saveMessage(MessageV1 messagePayload) {
        LocalDateTime createdAt = LocalDateTime.parse(messagePayload.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME);

        EventMessage message = eventMapper.toEntity(messagePayload);

        repository.save(message).doOnSuccess(savedMessage -> log.info("Saved message with ID: {}", savedMessage.getId()))
                .doOnError(error -> log.error("Error saving message", error)).subscribe();
    }

}
