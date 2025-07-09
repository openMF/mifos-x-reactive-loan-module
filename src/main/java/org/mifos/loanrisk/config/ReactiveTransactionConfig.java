package org.mifos.loanrisk.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class ReactiveTransactionConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory factory) {
        return new R2dbcTransactionManager(factory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager tm) {
        return TransactionalOperator.create(tm);
    }
}
