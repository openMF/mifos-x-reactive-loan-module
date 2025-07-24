package org.mifos.loanrisk.document.service.fetch;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.loanrisk.document.domain.DocumentMeta;
import org.mifos.loanrisk.document.domain.DocumentStatus;
import org.mifos.loanrisk.document.repository.DocumentMetaRepository;
import org.mifos.loanrisk.document.storage.ObjectStorageClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentFetchService {

    private final WebClient fineractClient;
    private final ObjectStorageClient storageClient;
    private final DocumentMetaRepository repository;
    private final TransactionalOperator txOperator;

    public Mono<DocumentMeta> fetch(String entityType, Long entityId, Long documentId) {
        String path = "/api/v1/%s/%d/documents/%d/attachment".formatted(entityType, entityId, documentId);
        return fineractClient.get().uri(path).retrieve().toEntity(byte[].class)
                .flatMap(resp -> {
                    String mime = resp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                    String key = "%s-%d".formatted(entityType, documentId);
                    DocumentMeta meta = new DocumentMeta(null, entityId, documentId, key, mime,
                            DocumentStatus.NEW, LocalDateTime.now());
                    return storageClient.put(resp.getBody(), key)
                            .then(repository.save(meta))
                            .as(txOperator::transactional);
                });
    }

    private Mono<Void> persistDocument(DocumentMeta meta) {
        return repository.save(meta)
                .doOnSuccess(saved -> log.info("Document metadata saved: {}", saved))
                .then();
    }
}
