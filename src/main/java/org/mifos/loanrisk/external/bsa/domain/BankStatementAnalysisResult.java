package org.mifos.loanrisk.external.bsa.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bank_statement_analysis_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankStatementAnalysisResult {

    @Id
    private Long id;

    @Column("loan_id")
    private Long loanId;

    @Column("attributes")
    private String attributes;
}
