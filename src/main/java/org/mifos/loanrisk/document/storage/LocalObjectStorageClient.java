package org.mifos.loanrisk.document.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalObjectStorageClient implements ObjectStorageClient {

    @Value("${app.document.storage.path:.documents}")
    private String basePath;

    private Path path(String key) {
        return Path.of(basePath, key);
    }

    @Override
    public Mono<Void> put(byte[] data, String key) {
        return Mono.fromRunnable(() -> {
            try {
                Files.createDirectories(Path.of(basePath));
                Files.write(path(key), data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store document", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<byte[]> get(String key) {
        return Mono.fromCallable(() -> Files.readAllBytes(path(key))).subscribeOn(Schedulers.boundedElastic());
    }
}
