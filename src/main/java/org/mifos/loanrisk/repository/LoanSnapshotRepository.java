package org.mifos.loanrisk.repository;

import org.mifos.loanrisk.domain.LoanSnapshot;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface LoanSnapshotRepository extends R2dbcRepository<LoanSnapshot, Long> {
    // derive queries
}
