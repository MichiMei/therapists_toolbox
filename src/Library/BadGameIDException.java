package Library;

public class BadGameIDException extends Exception {
    public BadGameIDException(int id) {
        super("ID " + id + "does not exist");
    }
}
