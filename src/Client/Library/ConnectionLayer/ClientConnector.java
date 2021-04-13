package Client.Library.ConnectionLayer;

import Host.Library.Controller.HostController;
import Library.ConnectionLayer.Address;
import Library.ContentClasses.UnimplementedException;
import Library.Protocol.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ClientConnector {

    private final Address address;
    private final String password;
    private final int version;
    private Socket socket;

    private final Logger logger;

    public Connection connect() throws IOException {
        // connect
        try {
            socket = new Socket(address.getIP(), address.getPort());
        } catch (ConnectException ex) {
            // TODO connection failed (Host not active or wrong address)
            return null;
        } catch (UnknownHostException ex) {
            // TODO non ip address could not be found
            return null;
        }

        Connection connection = null;
        try {
            connection = new Connection(socket);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("reached");
        }
        try {
            System.out.println("here");
            if (!connection.initiateCommunication()) {
                // TODO host rejected connection
                return null;
            }
        } catch (ConnectionClosedException e) {
            // TODO host closed connection
            return null;
        } catch (ProtocolViolationException e) {
            // TODO protocol violation by host
            return null;
        }
        return connection;
    }

    public ClientConnector(Address address, String password, int version) {
        logger = Logger.getLogger(ClientConnector.class.getName());
        this.address = address;
        this.password = password;
        this.version = version;
    }

    public class Connection {
        private Logger logger;

        private ObjectInputStream in;
        private ObjectOutputStream out;

        public Connection (Socket socket) throws IOException {
            logger = Logger.getLogger(Connection.class.getName());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }

        private boolean initiateCommunication() throws ConnectionClosedException, ProtocolViolationException {
            // send Hello
            MessageContent tmp = new MCHello(version);
            Message hello = new Message(tmp.getType(), tmp);
            sendMessage(hello);
            logger.info("hello send");

            // wait for Hello-Reply
            boolean pwRequired;
            Message msg = receiveMessage();
            if (msg.getType() == MCHelloReply.TYPE_ID) {    // parse reply
                MCHelloReply content = (MCHelloReply) msg.getContent();
                if (!content.isAccepted()) {                    // connection rejected
                    logger.info("hello-reply received -> rejected");
                    return false;
                }
                pwRequired = content.isPwRequired();
                logger.info("hello-reply received");
            } else if (msg.getType() == MCClose.TYPE_ID) {  // received close
                logger.warning("connection closed after hello");
                throw new ConnectionClosedException("Connection closed after hello: " + hello.toString());
            } else {                                        // received other message
                logger.warning("protocol violation after hello");
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
            if (msg.getType() == MCRegistrationAccept.TYPE_ID) {    // connection accepted
                logger.info("registration-accept received\nconnection established");
                return true;
            } else if (msg.getType() == MCClose.TYPE_ID) {          // connection rejected
                logger.warning("connection closed after registration");
                throw new ConnectionClosedException("Connection closed after registration: " + registration.toString());
            } else {                                                // received wrong message
                logger.warning("protocol violation after registration");
                throw new ProtocolViolationException("received wrong message: [" + msg.toString() + "]\nafter registration: [" + registration.toString() + "]");
            }
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
    }

}
