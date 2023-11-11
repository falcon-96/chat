package demo.chatservice.model;

import org.springframework.stereotype.Component;


public record Message(String from, String text, String time) {
}
