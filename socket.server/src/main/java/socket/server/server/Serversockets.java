package socket.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serversockets {
    private final Logger logger = LoggerFactory.getLogger(Serversockets.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(200);
    private ServerSocket serverSocket;

    public Serversockets(int port) {
        long start = System.currentTimeMillis();
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server Started | Time-taken: " + (System.currentTimeMillis() - start));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client Connected: " + clientSocket + " | Time-taken: " + (System.currentTimeMillis() - start));

                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Error accepting connection: " + e.getMessage() + " | Time-taken: " + (System.currentTimeMillis() - start));
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Serversockets server = new Serversockets(8900);
    }

    private void handleClient(Socket clientSocket) {
        long start = System.currentTimeMillis();
        try (
                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            while (true) {
                String line = dataInputStream.readUTF();
                String response = "Server Response: " + line;

                dataOutputStream.writeUTF(response);
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            logger.error("Error handling client: " + e.getMessage() + " | Time-taken: " + (System.currentTimeMillis() - start));
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing socket: " + e.getMessage() + " | Time-taken: " + (System.currentTimeMillis() - start));
            }
        }
    }
}
