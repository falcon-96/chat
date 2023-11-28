package demo.chatservice.service;

import demo.chatservice.model.Extra;
import demo.chatservice.model.Message;
import demo.chatservice.model.User;
import demo.chatservice.model.UserList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ChatAppService {

    private final SimpMessagingTemplate template;
    private UserList userList;

    public ChatAppService(UserList userList, SimpMessagingTemplate template) {
        this.userList = userList;
        this.template = template;
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
                publishNotification(user.getOriginal(), "change-name", user.getUsername());
            } else {
                userList.getUsers().add(user.getUsername());
                publishNotification(user.getUsername(), "connect", "");
                log.info(user.getUsername() + " Connected!");
            }

        } else {
            for (String username : userList.getUsers()) {
                if (username.equals(user.getUsername())) {
                    user.setConnected(-1);
                }
            }
            userList.getUsers().remove(user.getUsername());
            template.convertAndSend("/topic/extras",
                    processExtra(new Extra(user.getUsername(), "disconnect", "")));
            log.info(user.getUsername() + " Disconnected!");
        }

        log.info("List of connected users:" + userList.getUsers());
        return userList.getUsers();
    }

    public Extra processExtra(Extra extra) {
        switch (extra.getAction()) {
            case "typing":
                extra.setResult(extra.getName() + " is typing...");
                break;
            case "change-name":
                extra.setResult(extra.getName() + " changed name to " + extra.getResult());
                break;
            case "connect":
                extra.setResult((extra.getName() + " joined the arena."));
                break;
            case "disconnect":
                extra.setResult((extra.getName() + " left the arena."));
                break;
            default:
                log.error("Invalid or Unsupported action");
                break;
        }
        return extra;
    }

    private String getSanitizedMessage(String message) {
        log.warn("Cleaning message due to presence of '<' or '>' characters.");
        message = message.replace("<", "&lt;");
        message = message.replace(">", "&gt;");

        return message;
    }

    private void publishNotification(String username, String action, String result) {
        template.convertAndSend("/topic/extras",
                processExtra(new Extra(username, action, result)));
    }
}
