package org.mifos.loanrisk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
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

    public Mono<Aggregator> onLoanCreated(LoanAccountDataV1 loan) {
        // use aggregator.java's Aggregator(@NonNull LoanAccountDataV1 loan) method
        Aggregator ag = new Aggregator(loan);
        log.info("Creating new Aggregator for loan {}", loan.getId());
        return repo.existsByLoanId(loan.getId()).flatMap(exists -> {
            if (exists) {
                log.warn("Aggregator already exists for loan {}, skipping creation", loan.getId());
                return Mono.empty();
            }
            return repo.save(ag).doOnSuccess(savedAg -> log.info("Aggregator created: {}", savedAg));
        });
    }

    public Mono<Aggregator> getByLoanId(Long loanId) {
        return repo.findByLoanId(loanId);
    }

    public Mono<Void> onLoanUpdated(LoanAccountDataV1 loan) {
        return repo.findByLoanId(loan.getId())
                .switchIfEmpty(Mono.error(new IllegalStateException("No Aggregator row for loan " + loan.getId()))).flatMap(ag -> {
                    ag.updateFromLoan(loan);
                    return repo.save(ag).doOnSuccess(savedAg -> log.info("Aggregator updated: {}", savedAg))
                            .doOnError(error -> log.error("Error updating aggregator", error));
                }).then();
    }

    public Mono<Void> onLoanWithdrawn(LoanAccountDataV1 loan) {
        return repo.findByLoanId(loan.getId())
                .switchIfEmpty(Mono.error(new IllegalStateException("No Aggregator row for loan " + loan.getId()))).flatMap(ag -> {
                    ag.cancelLoan();
                    log.info("Cancelled Aggregator for loan {}", loan.getId());
                    return repo.save(ag).doOnSuccess(savedAg -> log.info("Aggregator cancelled: {}", savedAg))
                            .doOnError(error -> log.error("Error cancelling aggregator", error));
                }).then();
    }

    public Mono<Void> onLoanRejected(LoanAccountDataV1 loan) {
        return repo.findByLoanId(loan.getId())
                .switchIfEmpty(Mono.error(new IllegalStateException("No Aggregator row for loan " + loan.getId()))).flatMap(ag -> {
                    log.info("Loan Already rejected. Deleting Aggregator for loan {}", loan.getId());
                    return repo.delete(ag).doOnSuccess(v -> log.info("Aggregator deleted: {}", ag))
                            .doOnError(error -> log.error("Error deleting aggregator", error));
                }).then();
    }

    public Mono<Void> onDocumentCreated(DocumentDataV1 doc) {
        return processDocument(doc, /* added= */true);
    }

    public Mono<Void> onDocumentDeleted(DocumentDataV1 doc) {
        return processDocument(doc, /* added= */false);
    }

    /* core logic (shared) */

    private Mono<Void> processDocument(DocumentDataV1 doc, boolean added) {

        /* Validate parent type */
        if (!"loans".equalsIgnoreCase(doc.getParentEntityType())) {
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
                .flatMap(ag -> mutateAggregator(ag, dt, added, doc.getId())).flatMap(repo::save).then();
    }

    private Mono<Aggregator> mutateAggregator(Aggregator ag, DocumentType dt, boolean added, Long docId) {
        if (added) {
            switch (dt) {
                case BANK_STATEMENT -> {
                    if (ag.getBankStmtId() != null && !ag.getBankStmtId().equals(docId)) {
                        log.info("Replacing bank statement document {} with {}", ag.getBankStmtId(), docId);
                    }
                    ag.documentArrived(dt, docId);
                }
                case KYC_DOC -> {
                    if (ag.getKycDocId() != null && !ag.getKycDocId().equals(docId)) {
                        log.info("Replacing kyc document {} with {}", ag.getKycDocId(), docId);
                    }
                    ag.documentArrived(dt, docId);
                }
                case ID_DOC -> {
                    if (ag.getIdDocId() != null && !ag.getIdDocId().equals(docId)) {
                        log.info("Replacing id document {} with {}", ag.getIdDocId(), docId);
                    }
                    ag.documentArrived(dt, docId);
                }
            }
            log.info("Aggregator {} updated with new document {} of type {}", ag.getId(), docId, dt);
        } else {
            boolean isLatest = switch (dt) {
                case BANK_STATEMENT -> ag.getBankStmtId() != null && ag.getBankStmtId().equals(docId);
                case KYC_DOC -> ag.getKycDocId() != null && ag.getKycDocId().equals(docId);
                case ID_DOC -> ag.getIdDocId() != null && ag.getIdDocId().equals(docId);
            };
            if (isLatest) {
                ag.documentDeleted(dt);
                log.info("Aggregator {} removed document {} of type {}", ag.getId(), docId, dt);
            } else {
                log.info("document is not the latest one uploaded");
            }
        }
        return Mono.just(ag);
    }
}
