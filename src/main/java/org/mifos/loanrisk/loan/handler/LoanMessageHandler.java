package org.mifos.loanrisk.loan.handler;

import com.fasterxml.jackson.databind.JsonNode;

public interface LoanMessageHandler {

    void handle(JsonNode raw);
}
