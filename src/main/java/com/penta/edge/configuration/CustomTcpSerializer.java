package com.penta.edge.configuration;

import org.springframework.integration.ip.tcp.serializer.AbstractPooledBufferByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;
import org.springframework.integration.mapping.MessageMappingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CustomTcpSerializer extends AbstractPooledBufferByteArraySerializer {
    public static final CustomTcpSerializer INSTANCE = new CustomTcpSerializer();

    // 포맷 시작 문자 "{"  ,  끝문자 "}"

    public static final int STX_DEC = 123;
    public static final int STX_HEX = 0x7B;

    public static final int ETX_DEC = 125;
    public static final int ETX_HEX = 0x7D;

    @Override
    public byte[] doDeserialize(InputStream inputStream, byte[] buffer) throws IOException {
        int bite = inputStream.read();
        if (bite < 0) {
            throw new SoftEndOfStreamException("Serializer :: 빈 메시지");
        }
        buffer[0] = (byte) bite;
        int n = 1;
        try {
            if (bite != STX_DEC && bite != STX_HEX) {
                throw new MessageMappingException("Serializer :: Start of Text가 일치하지 않음");
            }
            while (true) {
                bite = inputStream.read();
                if(bite == -1) break;
                checkClosure(bite);
                buffer[n++] = (byte) bite;
                if (n >= getMaxMessageSize()) {
                    throw new IOException("ETX not found before max message length: " + getMaxMessageSize());
                }
            }
            if (buffer[n-1] != ETX_DEC && buffer[n-1] != ETX_HEX) {
                throw new MessageMappingException("Serializer :: End of Text가 일치하지 않음");
            }
            return copyToSizedArray(buffer, n);
        } catch (IOException e) {
            publishEvent(e, buffer, n);
            throw e;
        } catch (RuntimeException e) {
            publishEvent(e, buffer, n);
            throw e;
        }
    }

    @Override
    public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
    }
}
