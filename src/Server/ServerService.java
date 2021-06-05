package Server;

import Connection.Connection;

import java.util.HashMap;
import java.util.Map;


public class ServerService {

    private static Map<String, Connection> userList;

    public ServerService(){
        userList = new HashMap<>();
    }

    public void addUserToList(String login, Connection connection){
        userList.put(login, connection);
    }

    public void removeUserFromList(String login){
        userList.remove(login);
    }

    public void updateLogin(String oldLogin, String newLogin,Connection connection) {
        userList.remove(oldLogin);
        userList.put(newLogin,connection);
    }

    public Map<String, Connection> getUserList() {
        return userList;
    }
}
