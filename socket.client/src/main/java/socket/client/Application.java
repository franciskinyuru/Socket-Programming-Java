package socket.client;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import socket.client.echo.ServerUtil;
import socket.client.model.MessageRequest;

import static socket.client.discard.EchoClient.sendMessage;
import static socket.client.discard.EchoClient.startClient;

@SpringBootApplication

public class Application {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);

	}






}
