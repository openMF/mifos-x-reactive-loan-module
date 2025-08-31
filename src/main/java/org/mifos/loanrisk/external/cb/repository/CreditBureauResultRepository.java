package org.mifos.loanrisk.external.cb.repository;

import org.mifos.loanrisk.external.cb.domain.CreditBureauResult;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CreditBureauResultRepository extends R2dbcRepository<CreditBureauResult, Long> {
}
