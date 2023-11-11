package demo.chatservice.controller;

import demo.chatservice.model.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Message send( Message message) throws InterruptedException {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy'T'hh:mm:ss"));
        System.out.println("Message rec::"+message);
        return new Message("admin", "Hello, "+message.from(), time);
    }
}

