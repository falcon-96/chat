package demo.chatservice.service;

import demo.chatservice.model.Extra;
import demo.chatservice.model.Message;
import demo.chatservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class ChatAppService {

    List<String> list;

    public Message send(Message message) {
        String temp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
       // String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"));
        log.info("Message::" + message);
        if (!list.contains(message.from())) {
            handleUser(new User(message.from(), 1, "user", message.from()));
        }
        return new Message(message.from(), message.text(), temp, "message");
    }

    public List<String> handleUser(User user) {
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
                list.remove(user.getUsername());
                log.info(user.getUsername() + " Disconnected!");
            }
        }

        log.info("List of connected users:" + list);
        return list;
    }

    public Extra processExtra(Extra extra) {
        if (extra.getAction().equals("typing")) {
            extra.setResult(extra.getName() + " is typing...");
            return extra;
        }
        return extra;
    }
}
