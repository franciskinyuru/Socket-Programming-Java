package socket.client.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TcpConnectionManager {
    private final Map<String, SocketChannel> channelMap;

    public TcpConnectionManager() {
        this.channelMap = new HashMap<>();

        Thread connectionThread = new Thread(() -> {
            try {
                Selector selector = Selector.open();
                SocketChannel channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress("127.0.0.1", 8511));
                channel.register(selector, SelectionKey.OP_CONNECT);

                while (true) {
                    selector.select();

                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = keys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if (key.isConnectable()) {
                            SocketChannel connectedChannel = (SocketChannel) key.channel();
                            connectedChannel.finishConnect();
                            connectedChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            channelMap.put("tcpConnection", connectedChannel);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        connectionThread.start();
    }

    public Map<String, SocketChannel> getChannelMap() {
        return channelMap;
    }
}
