package Client.ConnectionLayer;

import Client.Controller.ClientController;
import Library.ConnectionLayer.Address;
import Library.Protocol.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * This class is used on client-side for connecting to a host
 * The main purpose is to create a Connection object for client <-> host communication
 */
public class ClientConnector {
    public static final int DEFAULT_PORT = 23432;

    private final Address address;
    private final String password;
    private final int version;

    private final Logger logger;

/*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

    /**
     * Create a Client Connector
     * @param address target host address
     * @param password password for connecting to the host
     * @param version client version (send to host to ensure compatibility)
     */
    public ClientConnector(Address address, String password, int version) {
        logger = Logger.getLogger(ClientConnector.class.getName());
        this.address = address;
        this.password = password;
        this.version = version;
    }

    /**
     * Creates Connection object by connecting to the host and establishing communication
     * [blocking]
     * @return Connection object able to send/receive messages to/from the host
     * @throws IOException thrown by socket operation, usually a severe problem resulting in program halt
     * @throws PasswordWrongException thrown if host refuses connection due to wrong password
     */
    public Connection connect() throws IOException, PasswordWrongException, ConnectionClosedException, ProtocolViolationException {
        // connect
        Socket socket;
        try {
            socket = new Socket(address.getIP(), address.getPort());
        } catch (ConnectException | UnknownHostException e) {
            logger.warning("connect:" + e.getMessage());
            e.printStackTrace();
            return null;
        }

        Connection connection = null;
        try {
            connection = new Connection(socket);
        } catch (IOException e) {
            logger.severe("Creating socket failed\n" + e.getMessage() + "\nshutting down");
            e.printStackTrace();
            System.exit(-1);
        }
        if (!connection.initiateCommunication()) {
            logger.warning("connection rejected");
            return null;
        }
        return connection;
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * This class represents the connection between a host and a client and is able to send/receive messages to/from
     * the host
     */
    public class Connection {
        private final Logger logger;
        private final Socket socket;
        // Object streams for message sending and receiving
        private final ObjectInputStream in;
        private final ObjectOutputStream out;

        /**
         * Create Connection object
         * Creates streams and prepares 'low-level' communication
         * [possibly blocking]
         * @param socket Socket-connection between client and host
         * @throws IOException thrown by socket operation, usually a severe problem resulting in program halt
         */
        public Connection (Socket socket) throws IOException {
            this.socket = socket;
            logger = Logger.getLogger(Connection.class.getName());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }

        /**
         * Establishes communication via communication initiation handshake
         * [blocking]
         * @return true, if communication established successfully
         * @throws ConnectionClosedException thrown if host closed socket surprisingly
         * @throws ProtocolViolationException thrown if host violated communication initiation protocol
         * @throws PasswordWrongException thrown if host refuses connection due to wrong password
         */
        private boolean initiateCommunication() throws ConnectionClosedException, ProtocolViolationException, PasswordWrongException {
            logger.info("initiating communication");

            // send Hello
            MessageContent tmp = new MCHello(version);
            Message hello = new Message(tmp.getType(), tmp);
            sendMessage(hello);
            logger.info("hello send");

            // wait for Hello-Reply
            boolean pwRequired;
            Message msg = receiveMessage();
            if (msg == null) return false;
            if (msg.getType() == MCHelloReply.TYPE_ID) {    // parse reply
                MCHelloReply content = (MCHelloReply) msg.getContent();
                pwRequired = content.isPwRequired();
                logger.info("hello-reply received");
            } else if (msg.getType() == MCClose.TYPE_ID) {  // received close
                logger.warning("connection closed after hello");
                close(0);
                throw new ConnectionClosedException("Connection closed after hello: " + hello.toString(), ((MCClose)msg.getContent()).getErrorCode());
            } else {                                        // received other message
                logger.warning("protocol violation after hello");
                close(2);
                throw new ProtocolViolationException("received wrong message: [" + msg.toString() + "]\nafter hello: [" + hello.toString() + "]");
            }

            // no password required -> finished
            if (!pwRequired) {
                logger.info("no password required\nconnection established");
                return true;
            }

            // send Registration
            tmp = new MCRegistration(password);
            Message registration = new Message(tmp.getType(), tmp);
            sendMessage(registration);
            logger.info("registration send");

            // wait for acceptance
            msg = receiveMessage();
            if (msg == null) return false;
            if (msg.getType() == MCRegistrationAccept.TYPE_ID) {    // connection accepted
                logger.info("registration-accept received\nconnection established");
                return true;
            } else if (msg.getType() == MCClose.TYPE_ID) {          // connection rejected
                if (((MCClose)msg.getContent()).getErrorCode() == 3) {
                    logger.warning("password wrong");
                    close(0);
                    throw new PasswordWrongException();
                }
                logger.warning("connection closed after registration");
                close(0);
                throw new ConnectionClosedException("Connection closed after registration: " + registration.toString(), ((MCClose)msg.getContent()).getErrorCode());
            } else {                                                // received wrong message
                logger.warning("protocol violation after registration");
                close(2);
                throw new ProtocolViolationException("received wrong message: [" + msg.toString() + "]\nafter registration: [" + registration.toString() + "]");
            }
        }

        /**
         * Send Message Object to host
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
         * Receive next message Object from host
         * [blocking]
         * @return Message Object
         */
        public Message receiveMessage() {
            Message msg = null;
            try {
                msg = (Message) in.readObject();
            } catch (EOFException e) {
                logger.info("eof reached");
                return null;
            } catch (SocketException e) {
                if (socket.isClosed()) {
                    logger.info("socket closed");
                    return null;
                } else {
                    logger.severe("receiveMessage(...) failed\n" + e.getMessage() + "\nshutting down");
                    e.printStackTrace();
                    System.exit(-1);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.severe("receiveMessage(...) failed\n" + e.getMessage() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
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
                socket.close();
                logger.info("socket closed");
            } catch (IOException e) {
                logger.severe("closing connection failed\n" + e.getMessage() + "\nshutting down");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Independent Message Receiver
     * Receives Messages and forwards them to the controller
     */
    public static class Receiver implements Runnable {
        private final Connection connection;
        private final ClientController controller;

        /**
         * Create Receiver Runnable
         * @param controller controller reference for incoming message notification
         * @param connection connection reference to receive messages
         */
        public Receiver(ClientController controller, Connection connection) {
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
     * Exception: Host refuses connection due to wrong password
     */
    public static class PasswordWrongException extends Exception {
        public PasswordWrongException() {
            super();
        }
    }
}
