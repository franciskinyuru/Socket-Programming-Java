package socket.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Service
public class SocketServices {
    private final Logger logger = LoggerFactory.getLogger(SocketServices.class);
    public String sendMessageToServer(String serverAddress, int serverPort, String message) {
            Long start = System.currentTimeMillis();
            try (Socket socket = new Socket(serverAddress, serverPort);
                 DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                String serverResponse = dataInputStream.readUTF();
                logger.info("Read message from server | Time-taken:" + (System.currentTimeMillis() - start));
                logger.info("Close connections and return response | Time-taken:" + (System.currentTimeMillis() - start));
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
                return serverResponse;
            } catch (IOException e) {
                logger.info("Error occurred | Time-taken: " + (System.currentTimeMillis() - start));
                return e.getMessage();
            }finally {

            }
        }
    }

