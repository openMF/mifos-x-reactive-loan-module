package org.mifos.loanrisk.utility;

import java.nio.ByteBuffer;
import org.springframework.stereotype.Component;

@Component
public class ByteBufferConvertor {

    public byte[] convert(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.position(buffer.position() - bytes.length);
        return bytes;
    }

    public ByteBuffer convert(byte[] buffer) {
        return ByteBuffer.wrap(buffer);
    }
}
