package com.nio.fix;

import java.io.IOException;

public class TestFlowChanged {
    public static void main(String[] args) {
        try {
            System.out.println("Starting app");
            ProcessingServer processingServer = new ProcessingServer("0.0.0.0", 5555);
            ManagingServer managingServer = new ManagingServer("0.0.0.0", 4444);
            managingServer.init();
            processingServer.init();

            ServerRegistry.startAllServers();

            Thread.sleep(5 * 60000); // wait some time for the main thread

            // Stop all servers
            ServerRegistry.stopAllServers();
        } catch (InterruptedException | IOException e) {
            System.out.println("Error occurred:" + e);
        }
    }

}
