package org.mifos.loanrisk.external.bsa;

import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.mifos.loanrisk.domain.Aggregator;
import reactor.core.publisher.Mono;

/**
 * Strategy interface for bank statement analysis providers. Implementations
 * encapsulate the details of talking to a particular third party API.
 */
public interface BankStatementAnalysisService {

    /**
     * @return unique provider name (e.g. "arya").
     */
    String getName();

    /**
     * Initiates analysis of the supplied bank statement document. The
     * Aggregator instance is provided so that implementations can update
     * service status or store metadata as needed.
     */
    Mono<Void> analyze(DocumentDataV1 document, Aggregator aggregator);
}

