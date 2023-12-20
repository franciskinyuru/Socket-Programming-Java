package socket.server.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servers {
    private ServerSocket serverSocket;

    public Servers(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server Started");
            System.out.println("Waiting for a Client..... ");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Accepted");

                // Create a new thread to handle each client
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Servers servers = new Servers(8900);
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
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
                    System.out.println("Client: " + line);

                    // Process the received message and generate a response
                    String response = "Server Response: " + line.toUpperCase();
                    // Send the response back to the client
                    dataOutputStream.writeUTF(response);
                    dataOutputStream.flush();
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    if (dataInputStream != null) dataInputStream.close();
                    if (dataOutputStream != null) dataOutputStream.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
