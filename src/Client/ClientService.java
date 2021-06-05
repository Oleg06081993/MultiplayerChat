package Client;

import java.util.HashSet;
import java.util.Set;

public class ClientService {

    private Set<String> loginList = new HashSet<>();

    public void refreshUserList(Set<String> loginList){
       this.loginList = loginList;
    }

    protected void addUserToList(String login){
        loginList.add(login);
    }

    protected void removeUserFromList(String login){
        loginList.remove(login);
    }

    protected void changeUserLogin(String oldLogin, String newLogin) {
        loginList.remove(oldLogin);
        loginList.add(newLogin);
    }

    protected void updateLogin(String oldLogin, String newLogin){
        loginList.remove(oldLogin);
        loginList.add(newLogin);
    }
    protected Set<String> getLoginList() {
        return loginList;
    }

    protected boolean checkIsLoginAvailableToChange(String newLogin) {
        if(loginList.contains(newLogin)  && newLogin.length() < 30 && newLogin != null) {
            return false;
        } else {
            return true;
        }
    }
}
