package com.mwitter.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {//içinde hata cevabı http durum kodu bulunan responseentity döndürüyor

        ErrorResponse errorResponse = new ErrorResponse(//gönderilecek json nesnesi
                LocalDateTime.now(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);//http kodunu yapıp oluşturuduğumuz hata nesnesini body olarak döndürüyor
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        List<String> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.add(error.getDefaultMessage());
        });

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                "Validation failed",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
