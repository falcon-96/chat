package demo.chatservice.controller;

import demo.chatservice.model.Message;
import demo.chatservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Controller
@Slf4j
public class ChatController {
    private List<String> list;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Message send(Message message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"));
        log.info("Message::" + message);
        if(!list.contains(message.from())){
            userStatus(new User(message.from(), 1, "user", message.from()));
        }
        return new Message(message.from(), message.text(), time, "message");
    }

    @MessageMapping("/users")
    @SendTo("/topic/users")
    public List<String> userStatus(User user) {
        System.out.println("in::" + user);
        if (list == null) {
            list = new LinkedList<>();
        } else {
            if (user.getConnected() == 1) {

                if (!user.getOriginal().equals(user.getUsername())) {
                    list.remove(user.getOriginal());
                    list.add(user.getUsername());
                    log.info(user.getOriginal() + " changed name to:" + user.getUsername());
                } else {
                    list.add(user.getUsername());
                    log.info(user.getUsername() + " Connected!");
                }

            } else {
                for (String username : list) {
                    if (username.equals(user.getUsername())) {
                        user.setConnected(-1);
                    }
                }
                List<String> duplicateList = new LinkedList<>(list);
                list.remove(user.getUsername());
                log.info(user.getUsername() + " Disconnected!");
            }
        }

        System.out.println("List of connected users:" + list);
        return list;
    }
}

