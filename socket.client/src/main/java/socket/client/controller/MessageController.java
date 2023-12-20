package socket.client.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import socket.client.model.MessageRequest;
import socket.client.service.SocketServices;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final SocketServices socketServices;
    public MessageController(SocketServices socketServices) {
        this.socketServices = socketServices;
    }
    @PostMapping("/send")
    public ResponseEntity<Object> sendMessage(@RequestBody MessageRequest message) {

        String resp = socketServices.sendMessageToServer("127.0.0.1", 8900, message.getMessage());
        return ResponseEntity.ok().body(resp);
    }
}
