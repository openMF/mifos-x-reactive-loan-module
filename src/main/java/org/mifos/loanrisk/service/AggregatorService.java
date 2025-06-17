package org.mifos.loanrisk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.mifos.loanrisk.document.common.DocumentType;
import org.mifos.loanrisk.domain.Aggregator;
import org.mifos.loanrisk.repository.AggregatorRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorService {

    private final AggregatorRepository repo;

    public Mono<Void> onDocumentCreated(DocumentDataV1 doc) {
        return processDocument(doc, /* added= */true);
    }

    public Mono<Void> onDocumentDeleted(DocumentDataV1 doc) {
        return processDocument(doc, /* added= */false);
    }

    /* core logic (shared) */

    private Mono<Void> processDocument(DocumentDataV1 doc, boolean added) {

        /* Validate parent type */
        if (!"loan".equalsIgnoreCase(doc.getParentEntityType())) {
            log.debug("Skipping document id={} (entityType={})", doc.getId(), doc.getParentEntityType());
            return Mono.empty();
        }

        /* Parse document type from name */
        DocumentType dt;
        try {
            dt = DocumentType.of(doc.getName());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown document name {}, skipping", doc.getName());
            return Mono.empty();
        }

        /* Fetch, mutate, save */
        return repo.findByLoanId(doc.getParentEntityId())
                .switchIfEmpty(Mono.error(new IllegalStateException("No Aggregator row for loan " + doc.getParentEntityId())))
                .flatMap(ag -> mutateAggregator(ag, dt, added)).flatMap(repo::save)
                .doOnSuccess(ag -> log.info("Aggregator {} updated: {} {}", ag.getId(), dt, added ? "added" : "removed")).then();
    }

    private Mono<Aggregator> mutateAggregator(Aggregator ag, DocumentType dt, boolean added) {
        if (added) {
            ag.documentArrived(dt);
        } else {
            ag.documentDeleted(dt);
        }
        return Mono.just(ag);
    }
}
