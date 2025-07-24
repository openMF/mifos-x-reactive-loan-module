package org.mifos.loanrisk.document.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("document_meta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMeta {

    @Id
    private Long id;

    @Column("loan_id")
    private Long loanId;

    @Column("document_id")
    private Long documentId;

    @Column("object_key")
    private String objectKey;

    @Column("mime_type")
    private String mimeType;

    @Column("status")
    private DocumentStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
