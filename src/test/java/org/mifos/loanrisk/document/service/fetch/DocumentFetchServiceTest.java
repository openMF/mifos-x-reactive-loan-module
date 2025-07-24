package org.mifos.loanrisk.document.service.fetch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.loanrisk.document.domain.DocumentMeta;
import org.mifos.loanrisk.document.domain.DocumentStatus;
import org.mifos.loanrisk.document.repository.DocumentMetaRepository;
import org.mifos.loanrisk.document.storage.ObjectStorageClient;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DocumentFetchServiceTest {

    private ObjectStorageClient storage;
    private DocumentMetaRepository repo;
    private TransactionalOperator txOp;
    private DocumentFetchService service;

    private AtomicReference<ClientRequest> captured;

    @BeforeEach
    void setUp() {
        storage = mock(ObjectStorageClient.class);
        repo = mock(DocumentMetaRepository.class);
        txOp = mock(TransactionalOperator.class);

        // Let transactional(Mono) / transactional(Flux) just return the original publisher
        when(txOp.transactional(Mockito.<Mono<?>>any())).then(AdditionalAnswers.returnsFirstArg());
        when(txOp.transactional(Mockito.<Flux<?>>any())).then(AdditionalAnswers.returnsFirstArg());

        when(storage.put(any(byte[].class), anyString())).thenReturn(Mono.empty());
        when(repo.save(any(DocumentMeta.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        captured = new AtomicReference<>();
    }

    @Test
    void fetch_ok_storesBytes_and_savesMeta_and_wrapsInTransaction() {
        // Arrange: a successful HTTP 200 with bytes and a content-type
        byte[] body = new byte[] { 1, 2, 3 };
        ExchangeFunction exchange = request -> {
            captured.set(request);
            return Mono.just(okResponse("image/png", body));
        };
        WebClient client = WebClient.builder().exchangeFunction(exchange).build();
        service = new DocumentFetchService(client, storage, repo, txOp);

        // Act + Assert
        StepVerifier.create(service.fetch("loans", 1L, 10L)).assertNext(meta -> {
            assertEquals(1L, meta.getLoanId());
            assertEquals(10L, meta.getDocumentId());
            assertEquals("loans-10", meta.getObjectKey());
            assertEquals("image/png", meta.getMimeType());
            assertEquals(DocumentStatus.NEW, meta.getStatus());
            assertNotNull(meta.getCreatedAt());
            assertTrue(meta.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        }).verifyComplete();

        // Verify request path
        assertEquals("/loans/1/documents/10/attachment", captured.get().url().getPath());

        // Verify side effects
        verify(storage).put(new byte[] { 1, 2, 3 }, "loans-10");
        verify(repo).save(any(DocumentMeta.class));

        // Verify transactional wrapping
        verify(txOp, times(1)).transactional(Mockito.<Mono<?>>any());
        verifyNoMoreInteractions(txOp);
    }

    @Test
    void fetch_httpError_bubblesUp_and_nothingIsPersisted() {
        // Arrange: simulate a 404
        ExchangeFunction exchange = request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        };
        WebClient client = WebClient.builder().exchangeFunction(exchange).build();
        service = new DocumentFetchService(client, storage, repo, txOp);

        // Act + Assert
        StepVerifier.create(service.fetch("loans", 1L, 10L)).expectError(WebClientResponseException.class).verify();

        // Ensure no storage nor repo save happened
        verify(storage, never()).put(any(), anyString());
        verify(repo, never()).save(any());
        verify(txOp, never()).transactional(Mockito.<Mono<?>>any());
    }

    @Test
    void fetch_repoFails_errorPropagates() {
        // Arrange
        byte[] body = new byte[] { 9, 9, 9 };
        ExchangeFunction exchange = request -> {
            captured.set(request);
            return Mono.just(okResponse("application/pdf", body));
        };
        WebClient client = WebClient.builder().exchangeFunction(exchange).build();
        service = new DocumentFetchService(client, storage, repo, txOp);

        when(repo.save(any(DocumentMeta.class))).thenReturn(Mono.error(new RuntimeException("DB down")));

        // Act + Assert
        StepVerifier.create(service.fetch("loans", 2L, 20L)).expectErrorMessage("DB down").verify();

        verify(storage).put(body, "loans-20");
        verify(repo).save(any(DocumentMeta.class));
        verify(txOp, times(1)).transactional(Mockito.<Mono<?>>any());
    }

    private static ClientResponse okResponse(String contentType, byte[] bytes) {
        DefaultDataBufferFactory f = new DefaultDataBufferFactory();
        DataBuffer buffer = f.wrap(bytes);
        return ClientResponse.create(HttpStatus.OK).header(HttpHeaders.CONTENT_TYPE, contentType).body(Flux.just(buffer)).build();
    }
}
