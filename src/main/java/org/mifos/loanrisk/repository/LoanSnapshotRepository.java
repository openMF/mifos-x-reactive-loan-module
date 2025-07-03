package org.mifos.loanrisk.repository;

import org.mifos.loanrisk.domain.LoanSnapshot;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface LoanSnapshotRepository extends R2dbcRepository<LoanSnapshot, Long> {

    Mono<LoanSnapshot> findByLoanId(Long loanId);

    Mono<Void> deleteByLoanId(Long loanId);

    Mono<Boolean> existsByLoanId(Long loanId);
}
