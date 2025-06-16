package org.mifos.loanrisk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;

@Configuration
public class MessageConsumerConfiguration {
    @Bean
    public DirectChannel inputChannel() {
        return new DirectChannel();
    }
}
