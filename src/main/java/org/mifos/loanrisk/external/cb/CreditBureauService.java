package org.mifos.loanrisk.external.cb;

import org.mifos.loanrisk.domain.Aggregator;
import reactor.core.publisher.Mono;

/**
 * Strategy interface for credit bureau providers.
 */
public interface CreditBureauService {

    /**
     * @return unique provider name (e.g. "isoftpull").
     */
    String getName();

    /**
     * Initiates credit bureau report pull for the given aggregator/loan.
     */
    Mono<Void> pull(Aggregator aggregator);
}
