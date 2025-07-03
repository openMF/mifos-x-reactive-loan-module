package org.mifos.loanrisk.repository;

import org.mifos.loanrisk.domain.Aggregator;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface AggregatorRepository extends R2dbcRepository<Aggregator, Long> {

    Mono<Boolean> existsByLoanId(Long loanId);

    Mono<Aggregator> findByLoanId(Long loanId);

    Mono<Aggregator> findOneByLoanId(Long loanId);

}
