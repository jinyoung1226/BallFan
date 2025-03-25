package BallFan.exception.auth;

public class BlackListedTokenException extends RuntimeException {
    public BlackListedTokenException(String message) {
        super(message);
    }
}
