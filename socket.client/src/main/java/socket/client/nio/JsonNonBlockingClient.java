package socket.client.nio;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class JsonNonBlockingClient implements Runnable {
    private final static String HOSTNAME = "127.0.0.1";
    private final static int PORT = 8511;

    private Selector selector;
    private final Map<CompletableFuture<String>, String> messageMap = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try {
            selector = Selector.open();
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(HOSTNAME, PORT));
            channel.register(selector, SelectionKey.OP_CONNECT);

            while (!Thread.interrupted()) {
                selector.select();

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) continue;

                    if (key.isConnectable()) {
                        connect(key);
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

    public void sendMessage(String message, CompletableFuture<String> responseHandler) {
        JsonObject jsonMessage = createJsonMessage(message);
        messageMap.put(responseHandler, message);

        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(HOSTNAME, PORT));

            while (!channel.finishConnect()) {
                // Wait for connection to establish
            }

            String jsonString = new Gson().toJson(jsonMessage);
            channel.write(ByteBuffer.wrap(jsonString.getBytes()));
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
            responseHandler.completeExceptionally(e);
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
    }

    private void read(SelectionKey key) throws IOException {
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
        String response = new String(buff);
        for (Map.Entry<CompletableFuture<String>, String> entry : messageMap.entrySet()) {
            if (response.contains(entry.getValue())) {
                entry.getKey().complete(response);
                messageMap.remove(entry.getKey());
                return;
            }
        }
    }

    private void close() {
        try {
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject createJsonMessage(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        return jsonObject;
    }
}
