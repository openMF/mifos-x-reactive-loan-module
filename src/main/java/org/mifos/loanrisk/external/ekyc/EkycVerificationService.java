package org.mifos.loanrisk.external.ekyc;

import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.mifos.loanrisk.domain.Aggregator;
import reactor.core.publisher.Mono;

/**
 * Interface for electronic KYC verification providers.
 */
public interface EkycVerificationService {

    String getName();

    /**
     * Initiates an eKYC verification for the supplied document/loan.
     */
    Mono<Void> verify(DocumentDataV1 document, Aggregator aggregator);
}

