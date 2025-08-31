package org.mifos.loanrisk.external.cb.repository;

import org.mifos.loanrisk.external.cb.domain.CreditBureauResult;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CreditBureauResultRepository extends R2dbcRepository<CreditBureauResult, Long> {

    Flux<CreditBureauResult> findAllByLoanId(Long loanId);
}
