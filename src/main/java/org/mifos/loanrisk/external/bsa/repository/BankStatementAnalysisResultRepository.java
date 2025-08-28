package org.mifos.loanrisk.external.bsa.repository;

import org.mifos.loanrisk.external.bsa.domain.BankStatementAnalysisResult;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface BankStatementAnalysisResultRepository extends R2dbcRepository<BankStatementAnalysisResult, Long> {
}
