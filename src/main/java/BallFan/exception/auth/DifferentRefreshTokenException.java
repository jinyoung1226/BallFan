package BallFan.exception.auth;

public class DifferentRefreshTokenException extends RuntimeException {
    public DifferentRefreshTokenException(String message) {
        super(message);
    }
}
