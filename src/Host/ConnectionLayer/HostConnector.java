package Host.ConnectionLayer;

import Host.Controller.HostController;
import Library.Protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * This class is used on host-side to listen for incoming client communication attempts
 * The main purpose is to create a Connection object for client <-> host communication
 */
public class HostConnector {
    public static final int DEFAULT_PORT = 23432;
    private String password;

    private final HostController controller;
    private final Logger logger;
    private ServerSocket serverSocket = null;
    private Connection connection = null;
    private Thread listener = null;

/*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

    /**
     * Create a Host connector to listen for incoming connection attempts
     * @param controller Controller reference for notification purposes
     */
    public HostConnector(HostController controller) {
        logger = Logger.getLogger(HostConnector.class.getName());
        this.controller = controller;
    }

    /**
     * Start listening for incoming transmissions
     * @param port listening port
     * @param password optional password for client verification
     * @throws IOException thrown by socket operation, usually a severe problem resulting in program halt
     */
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

    /**
     * Stop the listening process
     */
    public void stopListening() {
        assert (serverSocket != null);
        assert (listener != null);
        try {
            serverSocket.close();
            serverSocket = null;
            listener = null;
        } catch (Exception e) {
            logger.severe("stopListening failed\n" + e.getMessage() + "\nshutting down");
            System.exit(1);
        }
        logger.info("listening stopped");
    }

    /**
     * Disconnect from the client
     */
    public void disconnect(int errorCode) {
        assert (connection != null);
        connection.close(errorCode);
        connection = null;
        logger.info("disconnected");
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Called by listener to forward connection status changes
     * @param status new connection status
     * @param connection connection object (only if status==connected)
     * @param msg special message to display in GUI
     */
    private void statusChanged(Status status, Connection connection, String msg) {
        logger.info("status changed to " + status.name());
        if (status == Status.Connected) {
            this.connection = connection;
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                logger.severe("socket close failed\n" + e.getMessage() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
            this.serverSocket = null;
            this.listener = null;
        }
        controller.connectionStatus(status, connection, msg);
    }

    /**
     * This class represents the connection between a host and a client and is able to send/receive messages to/from
     * the client
     */
    public class Connection {
        private final Socket clientSocket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;

        private int clientVersion;

        /**
         * Create Connection object
         * Creates streams and prepares 'low-level' communication
         * [possibly blocking]
         * @param clientSocket Socket-connection between client and host
         * @throws IOException thrown by socket operation, usually a severe problem resulting in program halt
         */
        public Connection (Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        }

        /**
         * Establishes communication via communication initiation handshake
         * [blocking]
         * @return true if connection established successfully; false if password incorrect
         * @throws ProtocolViolationException thrown if client violated communication initiation protocol
         * @throws ConnectionClosedException thrown if host closed socket surprisingly
         */
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
            logger.info("hello received, version: " + getClientVersion());

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
                    throw new ConnectionClosedException("Connection closed after hello-reply: " + helloReply.toString(), ((MCClose)msg.getContent()).getErrorCode());
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

        /**
         * Send Message Object to client
         * @param msg Message Object
         */
        public void sendMessage(Message msg) {
            logger.info("send:\n" + msg.toString());
            try {
                out.writeObject(msg);
            } catch (IOException e) {
                logger.severe("sendMessage(...) failed\nMessage: " + msg.toString() + "\n" + e.getMessage() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
        }

        /**
         * Receive next message Object from client
         * [blocking]
         * @return Message Object
         */
        public Message receiveMessage() {
            Message msg = null;
            try {
                msg = (Message) in.readObject();
            } catch (SocketException e) {
                if (clientSocket.isClosed()) {
                    return null;
                } else {
                    logger.severe("receiveMessage(...) failed\n" + e.getMessage() + "\nshutting down");
                    e.printStackTrace();
                    System.exit(1);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.severe("receiveMessage(...) failed\n" + e.getMessage() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
            assert (msg != null);
            logger.info("received:\n" + msg.toString());
            return msg;
        }

        /**
         * Close connection by sending close message and closing the underlying socket
         * @param errorCode closing-reason to send to the client
         */
        public void close(int errorCode) {
            sendMessage(new Message(MCClose.TYPE_ID, new MCClose(errorCode)));
            try {
                clientSocket.close();
                statusChanged(Status.Offline, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("disconnected"));
            } catch (IOException e) {
                logger.severe("closing connection failed\n" + e.getMessage() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
            logger.info("connection closed, code: " + errorCode);
        }

        /**
         * Get the Clients version
         * @return client version
         */
        public int getClientVersion() {
            return clientVersion;
        }
    }

    /**
     * This class is used to Listen independently of other executions
     */
    class Listener implements Runnable {
        ServerSocket serverSocket;

        /**
         * Create a listener for this socket
         * @param serverSocket listening socket
         */
        public Listener(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        /**
         * Listen until fatal error occurs or connection attempt by a client
         */
        @Override
        public void run() {

            while (true) {
                // listen on socket and accept
                Socket tmp = null;
                try {
                    statusChanged(Status.Online, null, null);
                    tmp = serverSocket.accept();
                } catch (SocketException e) {      // Listening stopped -> end
                    logger.info("Listening stopped");
                    logger.info(e.getMessage());
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        logger.severe("closing socket failed\n" + e1.getMessage() + "\nshutting down");
                        e1.printStackTrace();
                        System.exit(1);
                    }
                    statusChanged(Status.Offline, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("listening_stopped"));
                    return;
                } catch (IOException e) {
                    logger.severe("Socket.accept failed\n" + e.getMessage() + "\nshutting down");
                    e.printStackTrace();
                    System.exit(-1);
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
                    logger.severe("connection creation failed\n" + e.getMessage() + "\nshutting down");
                    e.printStackTrace();
                    System.exit(-1);
                } catch (ProtocolViolationException e) {
                    logger.warning("Protocol Violation\n" + e.getMessage());
                    e.printStackTrace();
                    statusChanged(Status.Online, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("protocol_violation"));
                } catch (ConnectionClosedException e) {
                    logger.warning("Client closed connection\n" + e.getMessage());
                    e.printStackTrace();
                    statusChanged(Status.Online, null, ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("client_closed_connection"));
                }
            }
        }
    }

    /**
     * Independent Message Receiver
     * Receives Messages and forwards them to the controller
     */
    public static class Receiver implements Runnable {
        private final Connection connection;
        private final HostController controller;

        /**
         * Create Receiver Runnable
         * @param controller controller reference for incoming message notification
         * @param connection connection reference to receive messages
         */
        public Receiver(HostController controller, Connection connection) {
            this.controller = controller;
            this.connection = connection;
        }

        /**
         * Runnable: receive message and forward
         */
        @Override
        public void run() {
            while (true) {
                Message msg;
                msg = connection.receiveMessage();
                if (msg == null) break;
                controller.messageReceived(msg);
                if (msg.getType() == MCClose.TYPE_ID) break;
            }
        }
    }

    /**
     * Representing the current connection status
     */
    public enum Status {Offline, Online, Connecting, Connected}
}
