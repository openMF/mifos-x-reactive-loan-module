package org.mifos.loanrisk.document.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface DocumentMessageHandler {

    void handle(JsonNode raw) throws JsonProcessingException;
}
