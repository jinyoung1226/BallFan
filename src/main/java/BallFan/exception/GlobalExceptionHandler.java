package BallFan.exception;

import BallFan.dto.Response.ErrorResponse;
import BallFan.exception.auth.BlackListedTokenException;
import BallFan.exception.auth.DifferentRefreshTokenException;
import BallFan.exception.auth.DuplicatedSignUpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatedSignUpException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedSignUpException(Exception ex) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .message(ex.getMessage())
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DifferentRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleDifferentRefreshTokenException(DifferentRefreshTokenException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .message(e.getMessage())
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BlackListedTokenException.class)
    public ResponseEntity<ErrorResponse> handleBlackListedTokenException(BlackListedTokenException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .message(e.getMessage())
                .build(), HttpStatus.UNAUTHORIZED);
    }
}
