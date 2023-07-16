package Connection;

public enum MessageType {
    TEXT(""),
    NEW_USER(""),
    EXIT_USER(""),
    INFO_FROM_SERVER(""),
    CHANGE_LOGIN(""),
    UPDATE_USERLIST(""),
    CONFIRM_CLIENT_NAME(""),
    LOGIN_AVAILABLE("Логин свободен"),
    LOGIN_UNAVAILABLE("Логин занят другим пользователем");

    private final String textMessage;

    private MessageType(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getTextMessage() {
        return this.textMessage;
    }
}
