package Client;

import Connection.*;

import java.net.Socket;

public class Client implements Runnable {

    private static ClientGui clientGui;
    private static ClientService clientService;
    private Connection connection;
    private static boolean isClient;
    private static boolean isLoginConfirmed;
    private String currentLogin;
    private static Client client;

    public static void main(String[] args) {
        client = new Client();
        clientService = new ClientService();
        clientGui = new ClientGui(client, clientService);
        clientGui.goGuiClient();
    }

    @Override
    public void run() {
        createNewClient();
    }

    private void createNewClient() {
        if (!isClient) {
            try {
                String ip = clientGui.getIpField();
                int port = clientGui.getPortField();
                Socket socket = new Socket(ip, port);
                connection = new Connection(socket);
                processConfirmFromServer();
                while (!isLoginConfirmed) {
                    registerClientOnServer();
                }
                receiveMessage();
                connection.sendOutputMessage(new Message("К чату присоединился: ", currentLogin, MessageType.NEW_USER));
                clientGui.setButtonActivity(true,false,true, true);
                clientGui.updateChangeLoginButton("    " + currentLogin + "    ");
                clientGui.updateStatusButton("    Активен    ");
            } catch (Exception e) {
                clientGui.showClientErrorWindow("Не удалось подключиться к серверу");
            }
        }
    }

    protected void registerClientOnServer() throws InterruptedException{
            clientGui.startLoginRegistrationWindow();
            String login = clientGui.getLoginField();
            connection.sendOutputMessage(new Message("any text", login, MessageType.CONFIRM_CLIENT_NAME));
            Thread.sleep(1000);
    }

    private void processConfirmFromServer() {
        Thread incomeRegistrMessage = new Thread(()-> {
            try {
                while (!isClient) {
                    Message message = connection.getInputMessage();
                    if (message.getMessageType() == MessageType.LOGIN_AVAILABLE) {
                        isLoginConfirmed = true;
                        isClient = true;
                        currentLogin = message.getUserName();
                        clientGui.showClientInfoWindow(MessageType.LOGIN_AVAILABLE.getTextMessage());
                    }
                    if (message.getMessageType() == MessageType.LOGIN_UNAVAILABLE) {
                        clientGui.showClientInfoWindow(MessageType.LOGIN_UNAVAILABLE.getTextMessage());
                    }
                }
            } catch (Exception e) {
                clientGui.showClientErrorWindow("Сервер не доступен в настоящий момент");
                exitTheChat();
            }
        });
            incomeRegistrMessage.start();
    }

    protected void exitTheChat() {
        try {
            connection.sendOutputMessage(new Message(currentLogin + " покинул чат", currentLogin, MessageType.EXIT_USER));
            //connection.close();
            isClient = false;
            isLoginConfirmed = false;
            clientGui.setButtonActivity(false,true,false, false);
            clientGui.updateStatusButton("    Не активен    ");
            clientGui.updateChangeLoginButton("      Нет подключения к серверу      ");
        } catch (Exception e) {
            clientGui.showClientErrorWindow("Не удается разорвать подключение." + "\n" + "Попробуйте еще раз.");
        }
    }

    protected void changeLogin(String newLogin) {
        String oldLogin = currentLogin;
        currentLogin = newLogin;
        clientService.changeUserLogin(oldLogin,newLogin);
        connection.sendOutputMessage(new Message(oldLogin, currentLogin, MessageType.CHANGE_LOGIN));
        clientGui.updateChangeLoginButton("    " + newLogin + "    ");
    }

    private void receiveMessage() {

        Thread receiveThread = new Thread(new IncomingMessageThread());
        receiveThread.start();
    }

    protected void sendMessage(String text) {
                connection.sendOutputMessage(new Message(text, currentLogin, MessageType.TEXT));
    }

    protected String getCurrentLogin() {
        return currentLogin;
    }

    public static boolean getIsClient() {
        return isClient;
    }


    private class IncomingMessageThread implements Runnable {
        @Override
        public void run() {
            while (isClient) {
                try {
                    processIncomeMessage(connection.getInputMessage());
                } catch (Exception e) {
                    exitTheChat();
                    clientGui.showClientErrorWindow("Потеряна связь с сервером");
                }
            }
        }

        private void processIncomeMessage(Message message) {
            switch (message.getMessageType()) {
                case TEXT -> {
                    clientGui.updateChatArea(message.getUserName() + ": " + message.getText());
                    break;
                }
                case UPDATE_USERLIST -> {
                    clientService.refreshUserList(message.getListUsers());
                    clientGui.updateUserArea(clientService.getLoginList());
                    break;
                }
                case NEW_USER -> {
                    if (message.getUserName().equals(currentLogin)) {
                        clientGui.updateChatArea("Вы присоединились к чату под логином  " + currentLogin);
                        break;
                    }
                    clientGui.updateChatArea(message.getText() + message.getUserName());
                    clientService.refreshUserList(message.getListUsers());
                    break;
                }
                case EXIT_USER -> {
                    clientGui.updateChatArea("Пользователь " + message.getUserName() + " покинул чат");
                    clientService.refreshUserList(message.getListUsers());
                    break;
                }
                case INFO_FROM_SERVER -> {
                    clientGui.updateChatArea("Сервисное сообщение: " + message.getText());
                    break;
                }
                case CHANGE_LOGIN -> {
                    if (message.getUserName().equals(currentLogin)) {
                        clientGui.updateChatArea("Вы сменили логин на " + currentLogin);
                        break;
                    }
                    clientGui.updateChatArea("Пользователь " + message.getText() + " сменил логин на " + message.getUserName());
                    clientService.refreshUserList(message.getListUsers());
                    break;
                }
            }
        }

        /*private void processIncomeMessage(Message message) {

            if(message.getMessageType() == MessageType.TEXT) {
                clientGui.updateChatArea(message.getUserName() + ": " + message.getText());
            }
            if(message.getMessageType() == MessageType.UPDATE_USERLIST) {
                clientService.refreshUserList(message.getListUsers());
                clientGui.updateUserArea(clientService.getLoginList());
            }
            if(message.getMessageType() == MessageType.NEW_USER) {
                if(message.getUserName().equals(currentLogin)) {
                    clientGui.updateChatArea("Вы присоединились к чату под логином  " + currentLogin);
                    return;
                }
                clientGui.updateChatArea(message.getText() + message.getUserName());
                clientService.refreshUserList(message.getListUsers());
            }
            if(message.getMessageType() == MessageType.EXIT_USER) {
                clientGui.updateChatArea("Пользователь " + message.getUserName() + " покинул чат");
                clientService.refreshUserList(message.getListUsers());
            }
            if(message.getMessageType() == MessageType.INFO_FROM_SERVER) {
                clientGui.updateChatArea("Сервисное сообщение: " + message.getText());
            }
            if(message.getMessageType() == MessageType.CHANGE_LOGIN){
                if(message.getUserName().equals(currentLogin)){
                    clientGui.updateChatArea("Вы сменили логин на " + currentLogin);
                    return;
                }
                clientGui.updateChatArea("Пользователь "+ message.getText() + " сменил логин на " + message.getUserName());
                clientService.refreshUserList(message.getListUsers());
            }
        }*/
    }
}





