package socket.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedServers {
    private final Logger logger = LoggerFactory.getLogger(MultiThreadedServers.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    private ServerSocket serverSocket;

    public MultiThreadedServers(int port) {
        long start = System.currentTimeMillis();
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server Started | Time-taken: " + (System.currentTimeMillis() - start));
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client Connected:" + clientSocket + " | Time-taken: " + (System.currentTimeMillis() - start));
                // Submit a task to the thread pool for each client
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Error accepting connection: " + e.getMessage() + " | Time-taken: " + (System.currentTimeMillis() - start));
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        MultiThreadedServers server = new MultiThreadedServers(8900);
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try (
                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())
            ) {
                while (true) {
                    String line = dataInputStream.readUTF();
                    // Process the received message and generate a response
                    String response = "Server Response: " + line;
                    // Send the response back to the client
                    dataOutputStream.writeUTF(response);
                    logger.info("Server: finished writing data to client" + " | Time-taken: " + (System.currentTimeMillis() - start));
                    dataOutputStream.flush();
                }
            } catch (IOException e) {
                logger.info("Server disconnected: | Time-taken: " + (System.currentTimeMillis() - start));
            } finally {
                try {
                    // Close the streams and socket only once when the client disconnects
                    clientSocket.close();
                } catch (IOException e) {
                    logger.info("Connection Error:+" + e.getMessage() + " | Time-taken: " + (System.currentTimeMillis() - start));
                }
            }
        }
    }
}
