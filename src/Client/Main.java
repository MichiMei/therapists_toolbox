package Client;

import Library.ConnectionTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class Main {

    Logger logger;

    private Main() {
        logger = Logger.getLogger(Main.class.getName());
    }

    private void run() throws IOException {
        // check IPs
        ipCheck();

        // read ip and port from stdin
        BufferedReader stdinReader = new BufferedReader( new InputStreamReader( System.in ) );
        System.out.print("Insert destination IPv4: ");
        System.out.flush();
        String ip = stdinReader.readLine();
        System.out.print("Insert destination Port: ");
        System.out.flush();
        int port = Integer.parseInt(stdinReader.readLine());

        // open socket
        Socket socket = connect(ip, port);

        // create reader and writer
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);


    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.run();
    }

    private void ipCheck() throws IOException {
        ConnectionTools.getExternalIP();
        ConnectionTools.getLocalIP();
    }

    private Socket connect(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        logger.info("connected to " + socket.toString());
        return socket;
    }

}
