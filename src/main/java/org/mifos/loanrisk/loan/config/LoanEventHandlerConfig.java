package org.mifos.loanrisk.loan.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mifos.loanrisk.loan.common.Handles;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.mifos.loanrisk.loan.handler.LoanMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoanEventHandlerConfig {

    @Bean
    Map<LoanEventType, LoanMessageHandler> loanHandlers(List<LoanMessageHandler> list) {
        return list.stream()
                .collect(Collectors.toUnmodifiableMap(h -> h.getClass().getAnnotation(Handles.class).value(), Function.identity()));
    }
}
