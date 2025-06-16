package org.mifos.loanrisk.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.springframework.stereotype.Service;
import org.mifos.loanrisk.event.EventMessageDTO;
import org.mifos.loanrisk.domain.EventMessage;
import org.mifos.loanrisk.repository.EventMessageRepository;
import org.mifos.loanrisk.utility.ByteBufferConvertor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@AllArgsConstructor
@Slf4j
public class MessageConsumerServiceImpl implements MessageConsumerService {
    private final EventMessageRepository repository;
    private final ByteBufferConvertor byteBufferConvertor;

    @Override
    public Flux<EventMessageDTO> getMessages() {
        return repository.findAll()
                .flatMap(this::toDto)    // convert one by one
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<EventMessageDTO> getMessagesByType(String eventType) {
        return repository.findByType(eventType)
                .flatMap(this::toDto)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteMessages() {
        return repository.deleteAll();
    }


    /** Reflection is blocking; off-load to boundedElastic. */
    private Mono<EventMessageDTO> toDto(EventMessage msg) {
        return Mono.fromCallable(() -> convert(msg))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private EventMessageDTO convert(EventMessage msg)
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {

        Class<?> payloadClazz   = Class.forName(msg.getSchema());
        Method   fromByteBuffer = payloadClazz.getMethod("fromByteBuffer", ByteBuffer.class);
        Object   payload        = fromByteBuffer.invoke(null,
                byteBufferConvertor.convert(msg.getPayload()));

        /* -------- BULK EVENTS -------- */
        if ("BulkBusinessEvent".equalsIgnoreCase(msg.getType())) {
            Method datasMethod = payload.getClass().getMethod("getDatas");
            @SuppressWarnings("unchecked")
            List<BulkMessageItemV1> bulkItems =
                    (List<BulkMessageItemV1>) datasMethod.invoke(payload);

            String aggregatedPayload = bulkItems.stream()
                    .map(this::safeBulkConvert)
                    .collect(Collectors.joining(System.lineSeparator()));

            return dtoOf(msg, aggregatedPayload);
        }

        /* -------- SINGLE EVENT -------- */
        return dtoOf(msg, payload.toString());
    }


    private String iso(LocalDateTime ts) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ts);
    }

    private EventMessageDTO dtoOf(EventMessage src, String body) {
        return new EventMessageDTO(
                src.getEventId(),
                src.getType(),
                src.getCategory(),
                src.getTenantId(),
                iso(src.getCreatedAt()),
                body,
                src.getBusinessDate());
    }

    private String safeBulkConvert(BulkMessageItemV1 item) {
        try {
            Class<?> clazz = Class.forName(item.getDataschema());
            Method m       = clazz.getMethod("fromByteBuffer", ByteBuffer.class);
            Object pl      = m.invoke(null, item.getData());
            return pl.toString();
        } catch (Exception e) {
            log.error("Failed to parse bulk item {}", item.getDataschema(), e);
            return "<unparseable>";
        }
    }
}
