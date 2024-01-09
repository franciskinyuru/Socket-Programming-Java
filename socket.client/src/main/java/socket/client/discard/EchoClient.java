package socket.client.discard;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import socket.client.echo.EchoClientHandler;
import socket.client.echo.ServerUtil;

public final class EchoClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "400"));
    private static Channel channel; // Store the channel for sending messages

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx = ServerUtil.buildSslContext();
        // Start the client and store the channel
        channel = startClient(sslCtx);
        sendMessage("Hello francis hawayu");
    }

    public static Channel startClient(SslContext sslCtx) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Channel ch;
        try {
            Bootstrap b = new Bootstrap();
            String messageToSend = "Hello, server!";
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                            }
                            p.addLast(new EchoClientHandler());
                        }
                    });

            // Start the client and return the channel
            ChannelFuture f = b.connect(HOST, PORT).sync();
            return f.channel();
        } finally {
            // Don't shut down the event loop here, as we want to maintain the connection
        }
    }

    public static void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        } else {
            System.err.println("Channel is not active. Message not sent.");
        }
    }
}
