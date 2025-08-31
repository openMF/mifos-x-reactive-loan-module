package org.mifos.loanrisk.external.cb.domain;

import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("credit_bureau_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditBureauResult {

    @Id
    private Long id;

    @Column("loan_id")
    private Long loanId;

    @Column("attributes")
    private Json attributes;
}
