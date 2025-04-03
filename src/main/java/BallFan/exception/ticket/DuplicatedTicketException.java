package BallFan.exception.ticket;

public class DuplicatedTicketException extends RuntimeException {
    public DuplicatedTicketException(String message) {
        super(message);
    }
}
