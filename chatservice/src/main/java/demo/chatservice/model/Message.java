package demo.chatservice.model;

public record Message(String from, String text, String time, String messageType) {
}
