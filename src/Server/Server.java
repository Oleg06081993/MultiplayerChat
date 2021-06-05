package Server;

import Connection.*;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {

    private static ServerGui serverGui;
    private static ServerService serverService;
    private ServerSocket serverSocket;
    private static boolean isServer;

    protected static boolean getIsServer() {
        return isServer;
    }

    public static void main(String[] args) {
        Server server = new Server();
        serverService = new ServerService();
        serverGui = new ServerGui(server, serverService);
        serverGui.goGuiServer();
    }

    protected void startServer(int port) {
        try {
            if (!isServer) {
                serverSocket = new ServerSocket(port);
                isServer = true;
                serverGui.sendInformationServerMessage("Сервер успешно запущен на порту " + port + "\n");
                serverGui.setTextInUserArea("Ожидание подключений...");
                Thread newConnectionThread = new Thread(() -> {
                    try {
                        waitForNewConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                newConnectionThread.start();
            }
        } catch (Exception e) {
            serverGui.showServerErrorWindow("Не удалось запустить сервер. Проверьте наличие сети");
        }
    }

    protected void stopServer() {
        if (isServer) {
            try {
                isServer = false;
                for (Map.Entry<String, Connection> entry : serverService.getUserList().entrySet()) {
                    entry.getValue().sendOutputMessage(new Message("Сервер был остановлен", MessageType.INFO_FROM_SERVER));
                    entry.getValue().close();
                    // + Не забудь удалить Мапу юзеров
                }
                serverSocket.close();
                serverGui.sendInformationServerMessage("Сервер успешно остановлен");
                serverGui.setTextInUserArea("");
            } catch (Exception ex) {
                serverGui.showServerErrorWindow("Не удалось остановить сервер");
            }
        }
    }

    private void waitForNewConnection() throws IOException {
        while (isServer) {
            Socket newClientSock = serverSocket.accept();
            new MyServerThread(newClientSock).start();
        }
    }

    class MyServerThread extends Thread {

        private final Socket socket;
        private boolean isNameAvailable;
        private boolean isClient;

        public MyServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                Connection connection = new Connection(socket);
                      //загоняем в цикл регистрации имени
                processConfirmMessages(connection);
                      //выходим из него
                isClient = true;
                processMessageFromClient(connection);
            } catch (Exception ex) {
                serverGui.showServerErrorWindow("Не удалось подключить клиента");
            }
        }

        private void processConfirmMessages(Connection connection) throws IOException, ClassNotFoundException {
            while (isNameAvailable == false) {
                Message message = connection.getInputMessage();

                if (message.getMessageType() == MessageType.CONFIRM_CLIENT_NAME) {

                    String login = message.getUserName();

                    if (checkIsNameAvailable(login)) {
                        serverService.addUserToList(login, connection);
                        connection.sendOutputMessage(new Message("Ваш логин принят", login, MessageType.LOGIN_AVAILABLE));
                        isNameAvailable = true;
                        serverGui.updateUserArea(initializeUserList());
                        serverGui.sendInformationServerMessage("New user joined: name=" + login +
                                " , InetAddress=" + socket.getInetAddress() +
                                " , port=" + socket.getPort() + "\n");
                    } else {
                        connection.sendOutputMessage(new Message("Логин " + login + " уже занят", MessageType.LOGIN_UNAVAILABLE));
                    }
                }
            }
        }

        private boolean checkIsNameAvailable(String login) {

            for (Map.Entry<String, Connection> entry : serverService.getUserList().entrySet()) {
                if (entry.getKey() == login) {
                    return false;
                }
            }
            return true;
        }

        private void sendMessageEveryone(Message message) {
            for (Map.Entry<String, Connection> entry : serverService.getUserList().entrySet()) {
                entry.getValue().sendOutputMessage(message);
            }
        }

        private Set<String> initializeUserList() {
            Set<String> listUsers = new HashSet<>();
            for (Map.Entry<String,Connection> entry : serverService.getUserList().entrySet()) {
                listUsers.add(entry.getKey());
            }
            return listUsers;
        }

        private void processMessageFromClient(Connection connection) throws IOException, ClassNotFoundException {

            while (isClient) {
                Message message = connection.getInputMessage();

                if (message.getMessageType() == MessageType.TEXT) {
                    sendMessageEveryone(message);
                }
                if (message.getMessageType() == MessageType.NEW_USER) {
                    serverService.addUserToList(message.getUserName(), connection);
                    updateUserListActions(message);
                }
                if (message.getMessageType() == MessageType.EXIT_USER) {
                    serverService.removeUserFromList(message.getUserName());
                    updateUserListActions(message);
                    serverGui.sendInformationServerMessage("The user left: name=" + message.getUserName() +
                            " , InetAddress=" + socket.getInetAddress() +
                            " , port=" + socket.getPort() + "\n");
                    isClient = false;
                    connection.close();
                }
                if (message.getMessageType() == MessageType.CHANGE_LOGIN) {
                    serverService.updateLogin(message.getText(), message.getUserName(),connection);
                    updateUserListActions(message);
                }

            }
        }

        private void updateUserListActions(Message message) {
            sendMessageEveryone(message);
            Set<String> list = initializeUserList();
            sendMessageEveryone(new Message(list, MessageType.UPDATE_USERLIST));
            if(list.isEmpty()) {
                serverGui.setTextInUserArea("Ожидание подключений...");
            }else {
                serverGui.updateUserArea(list);
            }
        }
    }
}


