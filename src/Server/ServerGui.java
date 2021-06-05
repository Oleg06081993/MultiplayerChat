package Server;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ServerGui {
    private JFrame frame;
    private int port;
    private JTextField portField = new JTextField(10);
    private Server server;
    private ServerService serverService;
    private JTextArea informationArea;
    private JTextArea userArea;

    public ServerGui(Server server, ServerService serverService){
        this.server = server;
        this.serverService = serverService;
    }

    protected void goGuiServer(){
        frame = new JFrame("Multiplayer chat Server ");
        // информационная панель
        JPanel informPanel = new JPanel();
        informationArea = new JTextArea(15,48);
        informationArea.setEditable(false);
        informationArea.setLineWrap(true);
        informationArea.setText("Сервер не запущен"+ "\n" + "\n");
        JScrollPane informScrollPane = getScrollPane(informationArea);
        informScrollPane.setBorder(BorderFactory.createEtchedBorder(10,Color.gray,Color.DARK_GRAY));
        // тулбар
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton buttonQuantityUsers = new JButton();
        buttonQuantityUsers.setEnabled(false);
        toolBar.add(buttonQuantityUsers);
        Thread toolBarThread = new Thread(()->{
            while (true){
                buttonQuantityUsers.setText("Количество пользователей Online: " + serverService.getUserList().size());
            }
        });
        toolBarThread.start();
        toolBar.add(buttonQuantityUsers);
        toolBar.setFloatable(false);
        //панель пользователей
        JPanel userPanel = new JPanel();
        userArea = new JTextArea(15,17);
        userArea.setEditable(false);
        userArea.setLineWrap(true);
        userArea.setText("Нет активных пользователей");
        JScrollPane userScrollPane = getScrollPane(userArea);
        userScrollPane.setBorder(BorderFactory.createEtchedBorder(10,Color.gray,Color.DARK_GRAY));
        // панель кнопок
        JPanel buttonPanel = new JPanel();
        JButton buttonStartServer = new JButton("Запустить сервер");
        JButton buttonStopServer = new JButton("Остановить сервер");
        buttonStopServer.setEnabled(false);
        buttonStartServer.addActionListener(event -> {
                int port = getPortFromWindow();
                server.startServer(port);
            buttonStopServer.setEnabled(true);
            buttonStartServer.setEnabled(false);
        });

        buttonStopServer.addActionListener(event -> {
            server.stopServer();
            buttonStopServer.setEnabled(false);
            buttonStartServer.setEnabled(true);
        });
        // Время
        JLabel timeLabel = new JLabel();
        Thread timeThread = new Thread(()-> {
            while (true) {
                String time = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
                timeLabel.setText("                " + time);}
        });
        timeThread.start();

        buttonPanel.add(buttonStartServer);
        buttonPanel.add(buttonStopServer);
        buttonPanel.add(timeLabel);
        informPanel.add(informScrollPane);
        userPanel.add(userScrollPane);

        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setSize(790,370);
        frame.getContentPane().add(BorderLayout.NORTH,toolBar);
        frame.getContentPane().add(BorderLayout.WEST,informPanel);
        frame.getContentPane().add(BorderLayout.EAST,userPanel);
        frame.getContentPane().add(BorderLayout.SOUTH,buttonPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private int getPortFromWindow() {
        portField.setText("9050");
        int result = JOptionPane.showOptionDialog(frame,
                new Object[]{"Введите имя порта", portField},
                "Multiplayer chat", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (result == JOptionPane.OK_OPTION) {
            String text = portField.getText();
            port = Integer.parseInt(text);
        }
        return port;
    }

    protected void updateUserArea(Set<String> listUsers) {
        if(Server.getIsServer()) {
            userArea.setText("");
            StringBuilder list = new StringBuilder("  Активные пользователи:" + "\n");
            for(String user : listUsers) {
                list.append(user + "\n");
            }
            userArea.append(list.toString());
        }
    }

    protected void sendInformationServerMessage(String message){
        informationArea.append(message +"\n");
    }

    protected void showServerErrorWindow(String information){
        JOptionPane.showMessageDialog(null, information,"Multiplayer chat", JOptionPane.ERROR_MESSAGE);
    }

    private JScrollPane getScrollPane(JTextArea area){
        JScrollPane pane = new JScrollPane(area);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return pane;
    }

    protected void setTextInUserArea(String text) {
        userArea.setText("");
        userArea.setText(text);
    }

}
