package com.mwitter.exception;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(RuntimeException.class)
public ErrorResponse handleRuntimeException(RuntimeException ex) {

    return new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage()
        );

}
}
