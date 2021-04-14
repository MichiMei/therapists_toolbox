package Host.Library.ConnectionLayer;

import Host.Library.Controller.HostController;
import Library.ContentClasses.UnimplementedException;
import Library.Protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class HostConnector {
    public static final int DEFAULT_PORT = 23432;
    private String password;

    private final HostController controller;
    private final Logger logger;
    private ServerSocket serverSocket = null;
    private Connection connection = null;
    private Thread listener = null;

    public void startListening(int port, String password) throws IOException {
        assert (serverSocket == null);
        assert (listener == null);
        // open listening socket
        this.password = password;
        serverSocket = new ServerSocket(port);
        logger.info("Listening on " + serverSocket.toString());
        listener = new Thread(new Listener(serverSocket));
        listener.start();
        logger.info("Listener started");
    }

    public void stopListening() {
        assert (serverSocket != null);
        assert (listener != null);
        try {
            serverSocket.close();
            serverSocket = null;
            listener = null;
        } catch (Exception e) {
            logger.severe("stopListening failed\n" + e.toString() + "\nshutting down");
            System.exit(1);
        }
    }

    public void disconnect() {
        assert (connection != null);
        connection.close(1);
        connection = null;
    }

    public HostConnector(HostController controller) {
        logger = Logger.getLogger(HostConnector.class.getName());
        this.controller = controller;
    }

    private void statusChanged(Status status, Connection connection, String msg) {
        if (status == Status.Connected) {
            this.connection = connection;
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                logger.severe("socket close failed\n" + e.toString() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
            this.serverSocket = null;
            this.listener = null;
        }
        controller.connectionStatus(status, msg);
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
                close(2);
                throw new ProtocolViolationException("Client initiated communication without Hello: " + msg.toString());
            }
            logger.info("hello received");

            // send Hello-Reply
            boolean pwRequired = password != null;
            MessageContent tmp = new MCHelloReply(pwRequired);
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
                case MCRegistration.TYPE_ID -> {
                    MCRegistration content = (MCRegistration) msg.getContent();
                    if (!content.getPassword().equals(password)) {
                        logger.warning("password wrong");
                        close(3);
                        return false;
                    }
                }
                case MCClose.TYPE_ID -> {
                    logger.warning("connection closed after hello-reply");
                    throw new ConnectionClosedException("Connection closed after hello-reply: " + helloReply.toString());
                }
                default -> {
                    logger.warning("protocol violation after hello-reply");
                    close(2);
                    throw new ProtocolViolationException("received wrong message: [" + msg.toString() + "]\nafter hello-reply: [" + helloReply.toString() + "]");
                }
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

        public void close(int errorCode) {
            sendMessage(new Message(MCClose.TYPE_ID, new MCClose(errorCode)));
            try {
                clientSocket.close();
                statusChanged(Status.Offline, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("disconnected"));
            } catch (IOException e) {
                logger.severe("closing connection failed\n" + e.toString() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
        }

        public int getClientVersion() {
            return clientVersion;
        }
    }

    class Listener implements Runnable {
        ServerSocket serverSocket;

        public Listener(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {

            while (true) {
                // listen on socket and accept
                Socket tmp;
                try {
                    statusChanged(Status.Online, null, null);
                    tmp = serverSocket.accept();
                } catch (SocketException ex) {      // Listening stopped -> end
                    logger.info("Listening stopped");
                    logger.info(ex.toString());
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        logger.severe("closing socket failed\n" + e.toString() + "\nshutting down");
                        e.printStackTrace();
                        System.exit(1);
                    }
                    statusChanged(Status.Offline, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("listening_stopped"));
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO
                    throw new UnimplementedException("HostConnector::Listener::run() unimpl");
                }

                statusChanged(Status.Connecting, null, null);

                try {
                    Connection connection = new Connection(tmp);
                    if (!connection.initiateCommunication()) {
                        statusChanged(Status.Online, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("password_wrong_host_side"));
                        continue;
                    }
                    statusChanged(Status.Connected, connection, null);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO
                    throw new UnimplementedException("HostConnector::Listener::run() unimpl");
                } catch (ProtocolViolationException e) {
                    logger.warning("Protocol Violation\n" + e.toString());
                    e.printStackTrace();
                    statusChanged(Status.Online, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("protocol_violation"));
                } catch (ConnectionClosedException e) {
                    logger.warning("Client closed connection\n" + e.toString());
                    e.printStackTrace();
                    statusChanged(Status.Online, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("client_closed_connection"));
                }
            }
        }
    }

    public enum Status {Offline, Online, Connecting, Connected}
}
