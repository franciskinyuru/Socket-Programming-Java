package socket.client.nio;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.client.model.MessageRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NonBlockingClient {
    private Selector selector;
    private SocketChannel socketChannel;
    public final Logger logger = LoggerFactory.getLogger(NonBlockingClient.class);
    public void connect(String host, int port) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        while (!socketChannel.finishConnect()) {
            // Wait for the connection to be established
            logger.info("Connecting to server");
            try {
                Thread.sleep(10); // Adjust sleep duration as needed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for connection", e);
            }
        }
        logger.info("Connection status: " + socketChannel.isConnected());
    }

    public void send(MessageRequest message) throws IOException {
        try {
            long start = System.currentTimeMillis();
            log.info("sending message to server");
            String jsonMessage = new Gson().toJson(message); // Convert MessageRequest to JSON
            byte[] bytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            socketChannel.write(buffer);
            log.info("Finished writing to server | Time-taken " + (System.currentTimeMillis() - start));
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
public String resp() throws IOException {
    long start = System.currentTimeMillis();
    long timeout = 5000; // Timeout in milliseconds
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < timeout) {
        if (selector.select(timeout) == 0) {
            continue;
        }
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();

            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer buffers = ByteBuffer.allocate(1024);
                int bytesRead = channel.read(buffers);
                if (bytesRead > 0) {
                    buffers.flip();
                    byte[] receivedData = new byte[buffers.limit()];
                    buffers.get(receivedData);
                    log.info("Finished reading response | Time-taken " + (System.currentTimeMillis() - start));
                    return new String(receivedData, StandardCharsets.UTF_8);
                } else if (bytesRead == -1) {
                    channel.close();
                    key.cancel();
                    return "";
                }
            }
        }
    }
    // Timeout occurred waiting for a response
    log.info("No response received within the timeout");
    return "No response received within the timeout";
}

    public void close() throws IOException {
      logger.info(socketChannel.toString());
        if (selector != null) {
            selector.close();
        }
        // Close the socket channel
        if (socketChannel != null) {
            socketChannel.close();
        }
    }
    public static void main(String[] args) {
        try {
            NonBlockingClient client = new NonBlockingClient();
            MessageRequest messageRequest=new MessageRequest();
            messageRequest.setMessage("Hello from Nio Sockets");
            client.connect("127.0.0.1", 9800);
            client.send(messageRequest);
            String resp = client.resp();
            System.out.println(resp);
            // Close the client connection
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
