package Library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Logger;

public class ConnectionTools {

    public static String getExternalIP () throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        String ip = in.readLine(); //you get the IP as a String
        Logger logger = Logger.getLogger(ConnectionTools.class.getName());
        logger.info("Own external IP: " + ip);
        return ip;
    }

    public static String getLocalIP () throws IOException{
        final DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
        String ip = socket.getLocalAddress().getHostAddress();
        Logger logger = Logger.getLogger(ConnectionTools.class.getName());
        logger.info("Own local IP: " + ip);
        return ip;
    }

}
