package org.mifos.loanrisk.external.bsa.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published after a bank statement analysis request
 * has been sent to an external provider.
 */
public class BsaRequestedEvent extends ApplicationEvent {

    private final Long loanId;
    private final Long documentId;
    private final String requestId;

    public BsaRequestedEvent(Object source, Long loanId, Long documentId, String requestId) {
        super(source);
        this.loanId = loanId;
        this.documentId = documentId;
        this.requestId = requestId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getRequestId() {
        return requestId;
    }
}

