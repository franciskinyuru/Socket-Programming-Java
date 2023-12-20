package socket.server.server;

import lombok.SneakyThrows;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSide {
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(500);

    public ServerSide(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started");

            while (true) {
                System.out.println("Waiting for a client ...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);
                executorService.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } finally {
            try {
                serverSocket.close();
                executorService.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ServerSide(8900);
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @SneakyThrows
        @Override
        public void run() {
            try (
                DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
            ) {
                String line;
                while ((line = in.readUTF()) != null) {
                    System.out.println("Received from " + clientSocket + ": " + line);
                    // Process received data here
                    // Send a response back to the client
                    sendResponse(out, "Response to client: " + line);
                    clientSocket.close();
                    break; // Exit the loop to close the connection
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + clientSocket);

            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendResponse(DataOutputStream out, String message) throws IOException {
            out.writeUTF(message);
            out.flush();
        }
    }
}
