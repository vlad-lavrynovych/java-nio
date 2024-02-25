package com.nio.fix;

import java.util.ArrayList;
import java.util.List;

public class ServerRegistry {
    private static final List<AbstractTCPServer> servers = new ArrayList<>();

    public static void registerServer(AbstractTCPServer server) {
        servers.add(server);
    }

    public static void unregisterServer(AbstractTCPServer server) {
        servers.remove(server);
    }

    static void registerClientEvents(int operation) {
        servers.stream().filter(s -> !(s instanceof ManagingServer))
                .forEach(s -> s.registerEvent(operation));
    }

    public static void startAllServers() {
        servers.forEach(server -> {
            try {
                System.out.println("Starting server:" + server);
                server.start();
            } catch (Exception e) {
                System.out.println("Could not start server: " + server);
            }
        });
    }

    public static void stopAllServers() {
        servers.forEach(server -> {
            try {
                server.stop();
                unregisterServer(server);
            } catch (Exception e) {
                System.out.println("Could not stop server: " + server);
            }
        });
    }
}
