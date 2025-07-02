package org.mifos.loanrisk.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import org.apache.fineract.avro.MessageV1;
import org.mifos.loanrisk.messaging.domain.EventMessage;
import org.mifos.loanrisk.messaging.service.MessageConsumerService;
import org.mifos.loanrisk.utility.ByteBufferConvertor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventMapper {

    private final ObjectMapper om = new ObjectMapper();
    private final MessageConsumerService messageConsumerService;
    private final ByteBufferConvertor byteBufferConvertor;

    public EventEnvelope toEnvelope(MessageV1 msg) throws JsonProcessingException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {

        return new EventEnvelope(msg.getId(), EventCategory.valueOf(msg.getCategory()), msg.getType(), om.readTree(convertMsg(msg)),
                msg.getTenantId(), LocalDateTime.parse(msg.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME), msg.getBusinessDate());
    }

    public EventMessage toEntity(MessageV1 dto) {
        LocalDateTime ts = LocalDateTime.parse(dto.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME);
        return new EventMessage(dto.getId(), dto.getType(), dto.getCategory(), dto.getDataschema(), dto.getTenantId(), ts,
                byteBufferConvertor.convert(dto.getData()), // keep raw bytes
                dto.getBusinessDate());
    }

    private String convertMsg(MessageV1 msg)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> payloadClazz = Class.forName(msg.getDataschema());
        Method fromByteBuffer = payloadClazz.getMethod("fromByteBuffer", ByteBuffer.class);
        Object payload = fromByteBuffer.invoke(null, msg.getData());
        return payload.toString();
    }
}
