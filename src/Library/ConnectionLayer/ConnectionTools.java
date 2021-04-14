package Library.ConnectionLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;

public class ConnectionTools {

    public static String getExternalIP () throws IOException {
        URL whatIsIyIP = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatIsIyIP.openStream()));
        return in.readLine();
    }

    public static String getLocalIP () throws IOException{
        final DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
        return socket.getLocalAddress().getHostAddress();
    }

}
