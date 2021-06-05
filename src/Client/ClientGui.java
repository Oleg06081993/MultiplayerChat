package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ClientGui {

    private JTextArea userArea, chatArea, messageArea;
    private JTextField loginField, ipField, portField;
    private static Client client;
    private static ClientService clientService;
    private String login;
    private String ip;
    private int port;
    private boolean isDataCorrect;
    private JFrame frame;
    private JButton buttonSendMessage, buttonConnectToServer, buttonExitChat,
            buttonQuantityUsers, buttonChangeLogin, buttonUserStatus;

    public ClientGui(Client client, ClientService clientService) {
        this.client = client;
        this.clientService = clientService;
    }

    protected void goGuiClient() {

        //JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("Multiplayer chat");

        JPanel panel = new JPanel();
        chatArea = new JTextArea(30, 50);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScrollPane = getScrollPane(chatArea);
        panel.add(chatScrollPane, BorderLayout.WEST);

        userArea = new JTextArea(30, 15);
        chatArea.setEditable(false);
        userArea.setLineWrap(true);
        userArea.setEditable(false);
        JScrollPane userScrollPane = getScrollPane(userArea);
        panel.add(userScrollPane, BorderLayout.EAST);

        JPanel messagePanel = new JPanel();
        messageArea = new JTextArea(4, 30);
        messageArea.setLineWrap(true);
        JScrollPane messageScrollPane = getScrollPane(messageArea);

        buttonConnectToServer = new JButton("Присоединиться");
        buttonExitChat = new JButton("Покинуть чат");
        buttonExitChat.setEnabled(false);
        buttonSendMessage = new JButton("Отправить сообщение");
        buttonSendMessage.setEnabled(false);
        //  Кнопка Присоединиться к чату
        buttonConnectToServer.addActionListener(e -> {
            startClientPortWindow();
            Thread thread = new Thread(client);
            thread.start();
        });
        //  Кнопка Покинуть чат
        buttonExitChat.addActionListener(e -> {
            if(preventUserBeforeExit("Вы уверены, что хотите отключиться?") == 1){
                client.exitTheChat();
            }
        });
        //  Кнопка отправить сообщение
        buttonSendMessage.addActionListener(e -> {
            if (messageArea.getText().length() != 0 && !messageArea.getText().trim().isEmpty()) {
                client.sendMessage(messageArea.getText());
                messageArea.setText("");
            }
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        toolBar.setBackground(Color.GRAY);

        buttonQuantityUsers = new JButton();
        buttonQuantityUsers.setEnabled(false);
        buttonQuantityUsers.setText("Количество участников Online: ____");

        JLabel timeLabel = new JLabel();
        Thread timeThread = new Thread(() -> {
            while (true) {
                String time = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
                timeLabel.setText("     " + time + "     ");
            }
        });
        timeThread.start();

        buttonUserStatus = new JButton("Не активен");
        buttonChangeLogin = new JButton("      Нет подключения к серверу      ");
        buttonChangeLogin.setEnabled(false);
        buttonChangeLogin.addActionListener(e -> showChangeLoginWindow());

        toolBar.add(buttonUserStatus);
        toolBar.add(buttonChangeLogin);
        toolBar.add(timeLabel); // часы
        toolBar.add(buttonQuantityUsers);

        messagePanel.add(messageScrollPane);
        messagePanel.add(buttonSendMessage);
        messagePanel.add(buttonConnectToServer);
        messagePanel.add(buttonExitChat);

        frame.setResizable(false);
        frame.setVisible(true);
        frame.setSize(800, 650);
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(messagePanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(preventUserBeforeExit("Вы уверены, что хотите выйти из приложения?")==1){
                    client.exitTheChat();
                    System.exit(0);
                } else frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        });
    }

    private int preventUserBeforeExit(String message) {
        int result = showPreventExitWindow(message);
            if(result == JOptionPane.YES_OPTION){
                return 1;
            } else return 0;
    }

    private int showPreventExitWindow(String message) {
        String[] buttonLabels = new String[] {"Yes", "No"};
        String defaultOption = buttonLabels[0];
        Icon icon = null;

        return JOptionPane.showOptionDialog(frame,
                message,
                "Multiplayer chat",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                buttonLabels,
                defaultOption);
    }


    protected void setButtonActivity(boolean isSendMessage,boolean isConnectToServer,
                                     boolean isExitChat, boolean isChangeLogin) {
        buttonSendMessage.setEnabled(isSendMessage);
        buttonConnectToServer.setEnabled(isConnectToServer);
        buttonExitChat.setEnabled(isExitChat);
        buttonChangeLogin.setEnabled(isChangeLogin);
    }

    protected void updateChangeLoginButton(String text) {
        buttonChangeLogin.setText("    " + text + "    ");
    }

    protected void updateStatusButton(String text) {
        buttonUserStatus.setText("    " + text + "    ");
    }

    private JScrollPane getScrollPane(JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createEtchedBorder(10, Color.gray, Color.DARK_GRAY));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return pane;
    }

    private void startClientPortWindow() {
        ipField = new JTextField();
        ipField.setText("localhost");
        ipField.setToolTipText("Укажите IP адрес сервера. По умолчанию установлено значение localhost");
        portField = new JTextField();
        portField.setText("9050");
        portField.setToolTipText("Укажите номер порта, на котором сервер принимает новое подключение. " +
                "Корректным значением считается порт в диапазоне от 1025 до 65536");
        int result = JOptionPane.showOptionDialog(null,
                new Object[]{
                        "Введите IP адрес", ipField, "Введите номер порта", portField},
                "Подключение к серверу", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (result == JOptionPane.OK_OPTION) {
            do {
                ip = ipField.getText();
                port = Integer.parseInt(portField.getText());
                validateClientData();
            } while (!isDataCorrect);
        }
    }

    protected void startLoginRegistrationWindow(){
        loginField = new JTextField();
        loginField.requestFocus();
        loginField.setToolTipText("Необходимо ввести ваш Логин. Рекомендуется вводить логин латиницей без использования спецсимволов");
        int result = JOptionPane.showOptionDialog(null,
                new Object[]{
                        "Введите логин", loginField, },
                "Регистрация пользователя", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (result == JOptionPane.OK_OPTION) {
                login = loginField.getText();

        }
    }

    private void showChangeLoginWindow() {
        String info = "Текущий логин: " + client.getCurrentLogin() + "\n" + "\n" +
                "Введите новый логин:";
        JTextField newLoginField = new JTextField(20);
        newLoginField.setText(client.getCurrentLogin());
        newLoginField.selectAll();
        int result = JOptionPane.showOptionDialog(frame,
                new Object[]{info, newLoginField},
                "Смена логина", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            String newLogin = newLoginField.getText();
            if (clientService.checkIsLoginAvailableToChange(newLogin)) {
                client.changeLogin(newLogin);
            } else {
                showClientErrorWindow("Укажите корректный логин");
            }
        }
    }

    protected void validateClientData() {
        boolean isLoginCorrect = false;
        boolean isIpCorrect = false;
        boolean isPortCorrect = false;

        /*if (login.length() == 0) {
            if(clientService.getLoginList().size() == 0){
                setLogin("USER " + 1);
            }else {
                setLogin("USER " + (clientService.getLoginList().size()));
            }
            isLoginCorrect = true;
        }*/
        if(ip.length() == 0){
            isIpCorrect = true;
        }
        if(port < 1025 || port > 65536){
            isPortCorrect = true;
        }
        if(isIpCorrect == true|| isPortCorrect == true){
            showClientErrorWindow("Проверьте правильность указанных данных." +
                    " Используйте всплывающие подсказки при необходимости");
            isDataCorrect = false;
        } else {
            if(isLoginCorrect == true){
                showClientInfoWindow("Поскольку логин не был указан, будет присвоен логин по умолчанию");
            }
            isDataCorrect = true;
        }
    }

    protected void showClientErrorWindow(String information){
        JOptionPane.showMessageDialog(frame,information,"Multiplayer chat",JOptionPane.ERROR_MESSAGE);
    }

    protected void showClientInfoWindow(String information){
        JOptionPane.showMessageDialog(frame,information,"Multiplayer chat",JOptionPane.INFORMATION_MESSAGE);
    }

    protected void updateChatArea(String text){
        chatArea.append(text + "\n");
    }

    protected void setTextInUserArea(String text) {
        userArea.setText("");
        userArea.setText(text);
    }

    protected void updateUserArea(Set<String> userList) {
        if (Client.getIsClient()) {
            userArea.setText("");
            StringBuilder list = new StringBuilder("  Активные пользователи:" + "\n");
            for (String user : userList) {
                list.append(user + "\n");
            }
            userArea.append(list.toString());
            buttonQuantityUsers.setText("Количество участников Online: " + userList.size());
        }
    }

    protected String getLoginField() {
        return login;
    }

    protected String getIpField() {
        return ip;
    }

    protected int getPortField() {
        return port;
    }
}
