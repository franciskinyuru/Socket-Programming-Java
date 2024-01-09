package socket.client.httprequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import socket.client.model.MessageRequest;
import socket.client.nio.NonBlockingClient;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;
import socket.client.nio.TcpConnectionManager;
import socket.client.service.SocketServices;
@Slf4j
public class NonBlockingHttpServer {
   private final Logger logger = LoggerFactory.getLogger(NonBlockingClient.class);
    @Getter
    @Setter
    private static volatile String clientMessage = "";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, java.nio.channels.SocketChannel> channelMap;
    private static ThreadPoolTaskExecutor executor;
    public NonBlockingHttpServer(ThreadPoolTaskExecutor executor) throws IOException {
        this.executor = executor;
        TcpConnectionManager connectionManager=new TcpConnectionManager();
        channelMap = connectionManager.getChannelMap();
    }

    public NonBlockingHttpServer(Map<String, java.nio.channels.SocketChannel> channelMap, ThreadPoolTaskExecutor executor) {
        this.channelMap = channelMap;
        this.executor = executor;
    }

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(100);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer());

            int port = 8800;
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline()
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpObjectAggregator(1024 * 1024))
                    .addLast(new HttpServerHandler());
        }
    }

    private static class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final Logger logger = LoggerFactory.getLogger(NonBlockingClient.class);
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws JsonProcessingException, ExecutionException, InterruptedException {
            long start = System.currentTimeMillis();
            String uri = request.uri();
            System.out.println("Hello");
            if ("/message".equals(uri)) {
                String content = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
                logger.info("Received message from client: " );
                MessageRequest messageRequest = objectMapper.readValue(content, MessageRequest.class);

                Future<String> futureResponse = executor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        java.nio.channels.SocketChannel channel = channelMap.get("tcpConnection");
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.clear();
                        buffer.put(messageRequest.getMessage().getBytes());
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            channel.write(buffer);
                        }
                        Thread.sleep(3);
                        ByteBuffer readbuffer = ByteBuffer.allocate(1024);
                        int bytesRead = channel.read(readbuffer);
                        if (bytesRead == -1) {
                            // Connection closed
                            return null;
                        }
                        readbuffer.flip();
                        byte[] bytes = new byte[readbuffer.remaining()];
                        readbuffer.get(bytes);
                        return new String(bytes);
                    }
                });
                String resp = futureResponse.get();
                if (resp == null) {
                   resp="Failed";
                }
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                response.content().writeBytes(resp.getBytes(io.netty.util.CharsetUtil.UTF_8));
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                // Handle other endpoints or return an error response
                FullHttpResponse errorResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                ctx.writeAndFlush(errorResponse).addListener(ChannelFutureListener.CLOSE);
            }
            logger.info("Request receive and response sent back to customer | time taken: "+ (System.currentTimeMillis() -start) );
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

}
