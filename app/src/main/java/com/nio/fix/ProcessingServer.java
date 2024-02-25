package com.nio.fix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ProcessingServer extends AbstractTCPServer {

    public ProcessingServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    protected void handleRead(SocketChannel client, ByteBuffer buffer) throws IOException {
        String message = new String(buffer.array(), buffer.position(), buffer.remaining()).trim().toLowerCase();
        System.out.println("Received message: " + message);
        for (int i = 0; i < buffer.limit(); i++) {
            buffer.put(i, (byte) Character.toUpperCase(buffer.get(i)));
        }

        client.write(buffer);
        buffer.clear();
    }
}
