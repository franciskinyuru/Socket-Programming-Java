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

public class Server {
    private ServerSocket serverSocket;
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server Started");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client Connected: " + clientSocket);

                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8900);
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        @Override
        public void run() {
            try {
                dataInputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                while (true) {
                    String line = dataInputStream.readUTF();
                    String response = "Server Response: " + line;

                    dataOutputStream.writeUTF(response);
                    dataOutputStream.flush();
                }
            } catch (IOException e) {
                logger.error("Error handling client: " + e.getMessage());
            } finally {
                try {
                    if (dataInputStream != null) dataInputStream.close();
                    if (dataOutputStream != null) dataOutputStream.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    logger.error("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
