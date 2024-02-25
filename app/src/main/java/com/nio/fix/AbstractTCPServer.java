package com.nio.fix;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static sun.nio.ch.IOStatus.*;

public abstract class AbstractTCPServer implements Runnable {

    private static final int BUFFER_SIZE = 65535;

    private final String hostname;
    private final int port;
    private final ByteBuffer buffer;
    private final Map<Integer, List<SocketChannel>> clients;
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AbstractTCPServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.clients = new HashMap<>();
    }

    public void start() {
        Thread serverThread = new Thread(this);
        running.set(true);
        serverThread.start();
        System.out.println("Started server: " + this);
    }

    public void stop() throws IOException {
        running.set(false);
        selector.wakeup();
        serverSocket.close();
        selector.close();
    }

    public void init() throws IOException {
        System.out.println("Init server:" + this);
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(hostname, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ServerRegistry.registerServer(this);
    }

    void registerEvent(int op) {
        clients.values().stream().flatMap(Collection::stream).forEach(c -> {
            try {
                System.out.println("Registered operation:" + op + " for server " + this);
            } catch (Exception e) {
                System.out.println("Error occurred: " + e);
            }
        });
    }

    protected abstract void handleRead(SocketChannel client, ByteBuffer buffer) throws IOException;

    @Override
    public void run() {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        System.out.println("Handle ACCEPT event");
        SocketChannel client;
        try (ServerSocketChannel server = (ServerSocketChannel) key.channel()) {
            client = server.accept();
            clients.computeIfAbsent(client.socket().getPort(), k -> new ArrayList<>()).add(client);
            System.out.printf("New connection accepted: %s%n", client);
        }
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("Handle READ event");
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        int read = client.read(buffer);
        if (read == EOF) {
            client.close();
            key.cancel();
            return;
        }
        buffer.flip();
        handleRead(client, buffer);
    }

    @Override
    public String toString() {
        return "AbstractTCPServer{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", clients=" + clients +
                '}';
    }
}
