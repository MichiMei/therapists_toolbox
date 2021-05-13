package ConnectionTester;

import Protocol.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionTester {
    private final GUI gui;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public static void main(String[] args) {
        new ConnectionTester();
    }

    public ConnectionTester() {
        this.gui = new GUI(this);
    }

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }
        gui.connected();
    }

    public void listen(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }
        gui.connected();
    }

    public void send(Message msg) {
        try {
            System.out.println("snd: " + msg.toString());
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Message receive() {
        try {
            Message msg = (Message) in.readObject();
            System.out.println("rcv: " + msg.toString());
            return msg;
        } catch (IOException e) {
            if (!socket.isClosed()) {
                e.printStackTrace();
                System.exit(-1);
            }
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

}
