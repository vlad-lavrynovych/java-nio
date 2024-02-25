package com.nio.fix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ManagingServer extends AbstractTCPServer {

    private static final int OPERATION_STOP = 0;
    public static final String STOP_READ = "stop-read";
    public static final String START_READ = "start-read";
    private static final byte[] UNKNOWN_COMMAND = String.format("Supported commands:%n%s%n%s%n", STOP_READ, START_READ)
            .getBytes(StandardCharsets.UTF_8);

    private static final Map<String, Integer> OPERATIONS_MAP = Map.of(
            STOP_READ, OPERATION_STOP,
            START_READ, SelectionKey.OP_READ
    );

    public ManagingServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    protected void handleRead(SocketChannel client, ByteBuffer buffer) throws IOException {
        String command = new String(buffer.array(), buffer.position(), buffer.remaining()).trim().toLowerCase();

        System.out.println("Received command: " + command);

        Integer operation = OPERATIONS_MAP.get(command);

        if (operation != null) {
            System.out.println("Sending event: " + operation);
            ServerRegistry.registerClientEvents(operation);
        } else {
            client.write(ByteBuffer.wrap(UNKNOWN_COMMAND));
        }
    }
}
