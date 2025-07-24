package org.mifos.loanrisk.document.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.time.Duration;
import javax.net.ssl.SSLException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(WebClientConfig.FineractProps.class)
public class WebClientConfig {

    @Bean
    public WebClient fineractWebClient(FineractProps p) throws SSLException {
        HttpClient http = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) p.getConnectTimeout().toMillis())
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler((int) p.getReadTimeout().toSeconds())));

        if (p.getSsl().isTrustAll()) {
            SslContext ssl = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            http = http.secure(spec -> spec.sslContext(ssl));
        }

        return WebClient.builder().baseUrl(p.getBaseUrl()).defaultHeader("Fineract-Platform-TenantId", p.getTenantId())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(h -> h.setBasicAuth(p.getUsername(), p.getPassword())).clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }

    @Data
    @ConfigurationProperties(prefix = "fineract")
    public static class FineractProps {

        private String baseUrl;
        private String tenantId;
        private String username;
        private String password;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration readTimeout = Duration.ofSeconds(10);
        private Ssl ssl = new Ssl();

        @Data
        public static class Ssl {

            private boolean trustAll = false;
        }
    }
}
