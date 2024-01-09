package socket.client.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import socket.client.model.MessageRequest;
import socket.client.nio.TcpConnectionManager;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
@RestController
@RequestMapping("/message")
public class MessageController {
    private final Map<String, SocketChannel> channelMap;
    private final ThreadPoolTaskExecutor executor;
    public MessageController(ThreadPoolTaskExecutor executor) throws IOException {
        this.executor = executor;
        TcpConnectionManager connectionManager=new TcpConnectionManager();
        channelMap = connectionManager.getChannelMap();
    }


    @PostMapping("/send")
    public ResponseEntity<?> writeToSocket(@RequestBody MessageRequest message) throws ExecutionException, InterruptedException, ExecutionException {
        Future<String> futureResponse = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                SocketChannel channel = channelMap.get("tcpConnection");
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.clear();
                buffer.put(message.getMessage().getBytes());
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
        String response = futureResponse.get();
        if (response == null) {
            return ResponseEntity.status(500).body("Error processing request");
        }
        return ResponseEntity.ok().body(response);
    }
}
