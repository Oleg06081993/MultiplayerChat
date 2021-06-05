package Connection;
import java.io.*;
import java.net.Socket;

public class Connection implements Closeable{

    private final Socket socket;
    private final ObjectOutputStream ous;
    private final ObjectInputStream is;
    
    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.ous = new ObjectOutputStream(socket.getOutputStream());
        this.is = new ObjectInputStream(socket.getInputStream());
    }

    // сервер получает от кого-то сообщение
    public Message getInputMessage() throws IOException, ClassNotFoundException {
        synchronized (this.is) {
            Message message = (Message) is.readObject();
            return message;
        }
    }

    // и отправляет его всем участникам чата
    public void sendOutputMessage(Message message) {
        synchronized (this.ous) {
            try {
                ous.writeObject(message);
                ous.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        is.close();
        ous.close();
    }
}