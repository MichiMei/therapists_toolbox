package Host.Library.ConnectionLayer;

import Host.Library.Controller.HostController;
import Host.Main;
import Library.Protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class HostConnector {
    public static final int DEFAULT_PORT = 23432;
    private final int port;
    private final String password;

    private final HostController controller;
    private final Logger logger;
    private ServerSocket serverSocket;

    public Connection startListening() throws IOException {
        // open listening socket
        serverSocket = new ServerSocket(port);
        logger.info("Listening on " + serverSocket.toString());

        Socket tmp;
        // listen on socket and accept
        try {
            tmp = serverSocket.accept();
        } catch (SocketException ex) {
            logger.info("Listening stopped");
            logger.info(ex.toString());
            return null;
        }

        Connection connection = new Connection(tmp);
        try {
            if (!connection.initiateCommunication()) {
                // TODO wrong password
                return null;
            }
        } catch (ConnectionClosedException e) {
            // TODO client closed connection
            return null;
        } catch (ProtocolViolationException e) {
            // TODO protocol violation by client
            return null;
        }
        logger.info("Connected to " + tmp.toString());
        return connection;
    }

    public void stopListening() {
        try {
            System.out.println("11");
            serverSocket.close();
            System.out.println("12");
        } catch (Exception ex) {
            System.out.println("13");
            logger.severe("stopListening failed");
            logger.severe(ex.toString());
            logger.severe("shutting down");
            System.exit(1);
        }
    }

    public HostConnector(HostController controller, int port, String password) {
        logger = Logger.getLogger(Main.class.getName());
        this.controller = controller;
        this.port = port;
        this.password = password;
    }

    class Listener implements Runnable {

        @Override
        public void run() {

        }
    }

    public class Connection {
        private final Socket clientSocket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;

        private int clientVersion;

        public Connection (Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        }

        private boolean initiateCommunication() throws ProtocolViolationException, ConnectionClosedException {
            // wait for Hello
            Message msg = receiveMessage();
            if (msg.getType() == MCHello.TYPE_ID) {
                MCHello content = (MCHello) msg.getContent();
                clientVersion = content.getVersion();
                // TODO check if version matches
            } else {
                logger.warning("protocol violation (no hello)");
                throw new ProtocolViolationException("Client initiated communication without Hello: " + msg.toString());
            }
            logger.info("hello received");

            // send Hello-Reply
            boolean pwRequired = password != null;
            MessageContent tmp = new MCHelloReply(true, pwRequired);
            Message helloReply = new Message(tmp.getType(), tmp);
            sendMessage(helloReply);
            logger.info("hello-reply send");

            // no password required -> finished
            if (!pwRequired) {
                logger.info("no password required\nconnection established");
                return true;
            }

            // wait for Registration
            msg = receiveMessage();
            switch (msg.getType()) {
                case MCRegistration.TYPE_ID:
                    MCRegistration content = (MCRegistration) msg.getContent();
                    if (!content.getPassword().equals(password)) {
                        logger.warning("password wrong");
                        return false;
                    }
                    break;
                case MCClose.TYPE_ID:
                    logger.warning("connection closed after hello-reply");
                    throw new ConnectionClosedException("Connection closed after hello-reply: " + helloReply.toString());
                default:
                    logger.warning("protocol violation after hello-reply");
                    throw new ProtocolViolationException("received wrong message: [" + msg.toString() + "]\nafter hello-reply: [" + helloReply.toString() + "]");
            }
            logger.info("registration received");

            // send registration-Accept
            tmp = new MCRegistrationAccept();
            msg = new Message(tmp.getType(), tmp);
            sendMessage(msg);
            logger.info("registration-accept send\nconnection established");
            return true;
        }

        public void sendMessage(Message msg) {
            try {
                out.writeObject(msg);
            } catch (IOException e) {
                logger.severe("sendMessage(...) failed\nMessage: " + msg.toString() + "\n" + e.toString() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
        }

        public Message receiveMessage() {
            Message msg = null;
            try {
                msg = (Message) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                logger.severe("receiveMessage(...) failed\n" + e.toString() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
            assert (msg != null);
            return msg;
        }

        public int getClientVersion() {
            return clientVersion;
        }
    }
}
