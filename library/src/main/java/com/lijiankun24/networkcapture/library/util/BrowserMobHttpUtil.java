package com.lijiankun24.networkcapture.library.util;

import android.util.Log;

import com.google.common.net.HostAndPort;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.DecompressionException;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * Utility class with static methods for processing HTTP requests and responses.
 */
public class BrowserMobHttpUtil {

    private static final int DECOMPRESS_BUFFER_SIZE = 16192;

    public static String removeMatchingPort(String hostWithPort, int portNumber) {
        HostAndPort parsedHostAndPort = HostAndPort.fromString(hostWithPort);
        if (parsedHostAndPort.hasPort() && parsedHostAndPort.getPort() == portNumber) {
            return HostAndPort.fromHost(parsedHostAndPort.getHostText()).toString();
        } else {
            return hostWithPort;
        }
    }

    public static long getHeaderSize(HttpHeaders headers) {
        long headersSize = 0;
        for (Map.Entry<String, String> header : headers.entries()) {
            // +2 for ': ', +2 for new line
            headersSize += header.getKey().length() + header.getValue().length() + 4;
        }
        return headersSize;
    }

    public static byte[] extractReadableBytes(ByteBuf content) {
        byte[] binaryContent = new byte[content.readableBytes()];

        content.markReaderIndex();
        content.readBytes(binaryContent);
        content.resetReaderIndex();

        return binaryContent;
    }

    public static byte[] decompressContents(byte[] fullMessage) throws DecompressionException {
        InflaterInputStream gzipReader = null;
        ByteArrayOutputStream uncompressed;
        try {
            gzipReader = new GZIPInputStream(new ByteArrayInputStream(fullMessage));

            uncompressed = new ByteArrayOutputStream(fullMessage.length);

            byte[] decompressBuffer = new byte[DECOMPRESS_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gzipReader.read(decompressBuffer)) > -1) {
                uncompressed.write(decompressBuffer, 0, bytesRead);
            }

            fullMessage = uncompressed.toByteArray();
        } catch (IOException e) {
            throw new DecompressionException("Unable to decompress response", e);
        } finally {
            try {
                if (gzipReader != null) {
                    gzipReader.close();
                }
            } catch (IOException e) {
                Log.e("BrowserMobHttpUtil ", "Unable to close gzip stream" + e);
            }
        }
        return fullMessage;
    }
}
