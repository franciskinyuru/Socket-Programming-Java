package socket.client.sockets;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket socket = null;
    private BufferedReader input = null;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;

    private String address;
    private int port;

    public ClientSocket(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
            InputStream data = System.in;
            input = new BufferedReader(new InputStreamReader(System.in));
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        String line = "";
        while (!line.equals("Over")) {
            System.out.println("Enter a message (type 'Over' to exit):");
            try {
                line = input.readLine(); // Read input from console
                // Send the message to the server
                dataOutputStream.writeUTF(line);
                dataOutputStream.flush(); //
                String response = dataInputStream.readUTF();
                System.out.println("Server response: " + response);

            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        try {
            socket.close();
            dataOutputStream.close();
            input.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        ClientSocket clientSocket = new ClientSocket("127.0.0.1", 8900);

    }
}


