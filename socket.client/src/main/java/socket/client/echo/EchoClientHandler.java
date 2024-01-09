
package socket.client.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    public static final Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);
    long startTime = System.currentTimeMillis();

    private  ByteBuf firstMessage;

    /**
     * Creates a client-side handler.
     */
    public EchoClientHandler(String message) {
        firstMessage = Unpooled.buffer(message.length());
        firstMessage.writeCharSequence(message, StandardCharsets.UTF_8);
    }
    public EchoClientHandler(){}

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Writing message to server |"+(startTime));
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf receivedMessage = (ByteBuf) msg;
        String received = receivedMessage.toString(StandardCharsets.UTF_8);
        logger.info("Received response from server: "+received+" | Time-Taken: "+(System.currentTimeMillis() - startTime));
        receivedMessage.release();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
       ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}