package socket.client.echo;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

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
    private static Selector selector;
    private static SelectionKey key;
    private static SocketChannel socketChannel;
    public static void main(String[] args) throws IOException {
        NonBlockingClient nonBlockingClient=new NonBlockingClient();
        nonBlockingClient.connectToServer("127.0.0.1", 10000);
        nonBlockingClient.send("Hello server");
        nonBlockingClient.readData();
        System.out.println("Hello ");

    }

    public  void connectToServer(String host, int port) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
        selector = Selector.open();
        key=socketChannel.register(selector, SelectionKey.OP_CONNECT);
        key.interestOps(SelectionKey.OP_CONNECT);
        while (!socketChannel.finishConnect()) {
            // Wait for the connection to be established
            log.info("Connecting to server");
            try {
                Thread.sleep(10); // Adjust sleep duration as needed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for connection", e);
            }
        }
        log.info("Connection status: " + socketChannel.isConnected());
    }

    public void send(String message) throws IOException {
        long start = System.currentTimeMillis();
        log.info("sending message to server");
        String jsonMessage = new Gson().toJson(message); // Convert MessageRequest to JSON
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        socketChannel.write(buffer);
        log.info("Finished writing to server | Time-taken " + (System.currentTimeMillis() - start));
        socketChannel.register(selector, SelectionKey.OP_READ);
        key.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
        log.info("Registered read ops" );

    }

    public void readData() throws IOException {
        long start = System.currentTimeMillis();
        long timeoutDuration = 5000;
        while (true) {
            long elapsedTime = System.currentTimeMillis() - start;
            long remainingTime = timeoutDuration - elapsedTime;
            if (remainingTime <= 0) {
                // Timeout period has passed
                break;
            }
            if (selector.select(remainingTime) == 0) {
                // Handle timeout scenario or continue waiting
                continue;
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            System.out.println(selectedKeys);
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
                        log.info("Finished reading response | Time-taken " + (System.currentTimeMillis() - start) + " | Response: " + new String(receivedData, StandardCharsets.UTF_8));
                    } else if (bytesRead == -1) {
                        channel.close();
                        key.cancel();
                    }
                }
            }
        }
    }
}
