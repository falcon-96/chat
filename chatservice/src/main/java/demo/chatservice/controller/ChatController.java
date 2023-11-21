package demo.chatservice.controller;

import demo.chatservice.model.Extra;
import demo.chatservice.model.Message;
import demo.chatservice.model.User;
import demo.chatservice.service.ChatAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
public class ChatController {

    private final ChatAppService service;

    public ChatController(ChatAppService service) {
        this.service = service;
    }


    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Message send(Message message) {
        return service.send(message);
    }

    @MessageMapping("/users")
    @SendTo("/topic/users")
    public List<String> userStatus(User user) {
        //log.info("in::" + user);
        return service.handleUser(user);
    }

    @MessageMapping("/extra")
    @SendTo("/topic/extras")
    public Extra processExtra(Extra extra) {
        log.info(String.valueOf(extra));
        return service.processExtra(extra);
    }
}

