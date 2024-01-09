package socket.client.nio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
@Slf4j
public class JsonServer implements Runnable {
    private final static int PORT = 8511;
    private Selector selector;
    public JsonServer() {
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (!Thread.interrupted()) {
                selector.select(1000);

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        accept(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void close() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        long start=System.currentTimeMillis();
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length = channel.read(readBuffer);

        if (length == -1) {
            channel.close();
            key.cancel();
            return;
        }

        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        String jsonString = new String(buff).trim();
        // Simulate authentication (in a real-world scenario, perform actual authentication logic)
        String isAuthenticated = authenticate(jsonString);
        channel.write(ByteBuffer.wrap(isAuthenticated.getBytes()));
        log.info("Server read from client and write response | Time-taken: "+(System.currentTimeMillis()-start));
        key.interestOps(SelectionKey.OP_READ);
    }

    private String authenticate(String jsonMessage) {
       return jsonMessage+"Processed";
    }

    public static void main(String[] args) {
        JsonServer server = new JsonServer();
        Thread thread = new Thread(server);
        thread.start();
    }
}
