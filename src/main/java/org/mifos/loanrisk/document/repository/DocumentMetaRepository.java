package org.mifos.loanrisk.document.repository;

import org.mifos.loanrisk.document.domain.DocumentMeta;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DocumentMetaRepository extends R2dbcRepository<DocumentMeta, Long> {

    Mono<DocumentMeta> findByDocumentId(Long documentId);
}
