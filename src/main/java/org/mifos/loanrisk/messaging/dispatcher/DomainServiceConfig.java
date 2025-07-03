package org.mifos.loanrisk.messaging.dispatcher;

import java.util.Map;
import org.mifos.loanrisk.common.EventCategory;
import org.mifos.loanrisk.document.service.DocumentEventService;
import org.mifos.loanrisk.loan.service.LoanEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DomainServiceConfig {

    @Bean
    Map<EventCategory, DomainEventService> domainServices(LoanEventService loanSvc, DocumentEventService docSvc) {
        return Map.of(EventCategory.Loan, loanSvc, EventCategory.DOCUMENT, docSvc);
    }
}
