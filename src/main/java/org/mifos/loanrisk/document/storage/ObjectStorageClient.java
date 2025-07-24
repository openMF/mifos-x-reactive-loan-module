package org.mifos.loanrisk.document.storage;

import reactor.core.publisher.Mono;

public interface ObjectStorageClient {

    Mono<Void> put(byte[] data, String key);

    Mono<byte[]> get(String key);
}
