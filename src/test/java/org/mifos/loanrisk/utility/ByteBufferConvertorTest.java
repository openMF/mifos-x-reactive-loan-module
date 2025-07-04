package org.mifos.loanrisk.utility;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteBufferConvertorTest {

    private final ByteBufferConvertor convertor = new ByteBufferConvertor();

    @Test
    void convertByteBufferToBytesAndBack() {
        String json = """
                {"id":2009,"accountNo":"000002009","externalId":null,"externalOwnerId":null,
                "settlementDate":null,"purchasePriceRatio":null,
                "status":{"id":100,"code":"loanStatusType.submitted.and.pending.approval",
                "value":"Submitted and pending approval","pendingApproval":true,
                "waitingForDisbursal":false,"active":false,"closedObligationsMet":false,
                "closedWrittenOff":false,"closedRescheduled":false,"closed":false,"overpaid":false},
                "subStatus":null,"clientId":1246,"clientAccountNo":"000001246","clientName":"loanperson eventtwo",
                "clientOfficeId":1,"clientExternalId":null,"loanProductId":1382,
                "loanProductName":"LOAN_PRODUCT_01N76G","loanProductDescription":null,
                /* … rest of JSON unchanged … */
                "customData":{}}
                """;

        byte[] original = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ByteBuffer buffer = convertor.convert(original);
        byte[] extracted = convertor.convert(buffer);

        String roundTrip = new String(extracted, java.nio.charset.StandardCharsets.UTF_8);

        assertArrayEquals(original, extracted); // byte-level check
        assertEquals(json, roundTrip); // human-readable check
    }
}
