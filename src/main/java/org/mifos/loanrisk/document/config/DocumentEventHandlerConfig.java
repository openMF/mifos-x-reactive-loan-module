package org.mifos.loanrisk.document.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mifos.loanrisk.document.common.DocumentEventType;
import org.mifos.loanrisk.document.common.Handles;
import org.mifos.loanrisk.document.handler.DocumentMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentEventHandlerConfig {

    @Bean
    Map<DocumentEventType, DocumentMessageHandler> documentHandlers(List<DocumentMessageHandler> list) {
        return list.stream()
                .collect(Collectors.toUnmodifiableMap(h -> h.getClass().getAnnotation(Handles.class).value(), Function.identity()));
    }
}
