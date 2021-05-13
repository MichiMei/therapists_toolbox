package ConnectionLayer;

public class Address {
    private final String ip;
    private final int port;
    private final boolean IPv4;

    public Address(String ip, int port) throws BadIPAddressException {
        this.ip = ip;
        this.IPv4 = !ip.contains(":");
        if (port < 0 || port > 65535) throw new BadIPAddressException("Port out of Range (" + port + ")");
        this.port = port;
    }

    /**
     *
     * @param text IPv4 w.x.y.z:port; IPv6 [s:t:u:v:w:x:y:z]:port
     */
    public Address(String text) throws BadIPAddressException {
        if (text.charAt(0) == '[') {    // IPv6
            int end = text.indexOf(']');
            ip = text.substring(1, end);
            this.IPv4 = false;
            int start = text.indexOf(':', end);
            try {
                port = Integer.parseInt(text.substring(start + 1));
            } catch (NumberFormatException ex) {
                throw new BadIPAddressException("failed to parse port (" + text.substring(start + 1) + "): " + ex.toString());
            }
        } else {                        // IPv4
            int separator = text.indexOf(':');
            ip = text.substring(0, separator);
            this.IPv4 = true;
            try {
                port = Integer.parseInt(text.substring(separator + 1));
            } catch (NumberFormatException ex) {
                throw new BadIPAddressException("failed to parse port (" + text.substring(separator + 1) + "): " + ex.toString());
            }
        }
        if (port < 0 || port > 65535) throw new BadIPAddressException("Port out of Range (" + port + ")");
    }

    public String getIP() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public String toString() {
        if (IPv4) return this.ip + ':' + this.port;
        else return "[" + this.ip + "]:" + this.port;
    }

    public static class BadIPAddressException extends Exception {
        public BadIPAddressException(String message) {
            super(message);
        }
    }
}
