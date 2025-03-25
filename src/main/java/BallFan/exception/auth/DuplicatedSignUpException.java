package BallFan.exception.auth;

public class DuplicatedSignUpException extends RuntimeException {
    public DuplicatedSignUpException(String message) {
        super(message);
    }
}
