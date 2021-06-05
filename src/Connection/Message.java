package Connection;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Message implements Serializable {

    private String text;
    private MessageType messageType;
    private String userName;
    private Set<String> listUsers;

    public Message(Set<String> listUsers, MessageType messageType){
        if(listUsers == null) {
            listUsers = new HashSet<>();
        }
        this.listUsers = listUsers;
        this.messageType = messageType;
    }

    public Message(String text, MessageType messageType){
        this.text = text;
        this.messageType = messageType;
    }

    public Message(String text, String userName,MessageType messageType){
        this.text = text;
        this.userName = userName;
        this.messageType = messageType;
    }

    public String getText(){
        return text;
    }

    public String getUserName(){
        return userName;
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public Set<String> getListUsers() {
        return listUsers;
    }
}
