package org.mifos.loanrisk.loan.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.mifos.loanrisk.loan.common.Handles;
import org.mifos.loanrisk.loan.common.LoanEventType;
import org.springframework.stereotype.Component;

@Component
@Handles(LoanEventType.CREATED)
public class LoanCreatedHandler implements LoanMessageHandler {

    @Override
    public void handle(JsonNode raw) {
        // LoanCreatedDTO dto = convert(raw);
        // domain logic (store aggregate, publish further events, etc.)
    }
}
