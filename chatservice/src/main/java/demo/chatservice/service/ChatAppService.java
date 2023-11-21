package demo.chatservice.service;

import demo.chatservice.model.Extra;
import demo.chatservice.model.Message;
import demo.chatservice.model.User;
import demo.chatservice.model.UserList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ChatAppService {

    private UserList userList;

    public ChatAppService(UserList userList) {
        this.userList = userList;
    }


    public Message send(Message message) {
        String temp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        log.info("Message::" + message);
        String sanitizedMessage = getSanitizedMessage(message.text());
        if (!userList.getUsers().contains(message.from())) {
            handleUser(new User(message.from(), 1, "user", message.from()));
        }
        return new Message(message.from(), sanitizedMessage, temp, "message");
    }

    public List<String> handleUser(User user) {

        if (user.getConnected() == 1) {

            if (!user.getOriginal().equals(user.getUsername())) {
                userList.getUsers().remove(user.getOriginal());
                userList.getUsers().add(user.getUsername());
                log.info(user.getOriginal() + " changed name to:" + user.getUsername());
            } else {
                userList.getUsers().add(user.getUsername());
                log.info(user.getUsername() + " Connected!");
            }

        } else {
            for (String username : userList.getUsers()) {
                if (username.equals(user.getUsername())) {
                    user.setConnected(-1);
                }
            }
            userList.getUsers().remove(user.getUsername());
            log.info(user.getUsername() + " Disconnected!");
        }

        log.info("List of connected users:" + userList.getUsers());
        return userList.getUsers();
    }

    public Extra processExtra(Extra extra) {
        if (extra.getAction().equals("typing")) {
            extra.setResult(extra.getName() + " is typing...");
            return extra;
        }
        return extra;
    }

    private String getSanitizedMessage(String message) {
        log.warn("Cleaning message due to presence of '<' or '>' characters.");
        message = message.replace("<", "&lt;");
        message = message.replace(">", "&gt;");

        return message;
    }
}
