package org.mifos.loanrisk.document.common;

import java.util.Arrays;

public enum DocumentType {

    BANK_STATEMENT("bankStatement"), ID_DOC("idDocument"), KYC_DOC("kycDocument");

    private final String wireName;

    DocumentType(String wireName) {
        this.wireName = wireName;
    }

    /** Reverse-lookup from the wire value (case-insensitive). */
    public static DocumentType of(String raw) {
        return Arrays.stream(values()).filter(d -> d.wireName.equalsIgnoreCase(raw)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown document name: " + raw));
    }
}
