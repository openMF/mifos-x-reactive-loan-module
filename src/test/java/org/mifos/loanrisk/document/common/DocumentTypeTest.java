package org.mifos.loanrisk.document.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DocumentTypeTest {

    @Test
    void ofReturnsMatchingEnum() {
        assertEquals(DocumentType.BANK_STATEMENT, DocumentType.of("bankStatement"));
        assertEquals(DocumentType.ID_DOC, DocumentType.of("idDocument"));
        assertEquals(DocumentType.KYC_DOC, DocumentType.of("kycDocument"));
    }

    @Test
    void ofThrowsForUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> DocumentType.of("bogus"));
    }
}
