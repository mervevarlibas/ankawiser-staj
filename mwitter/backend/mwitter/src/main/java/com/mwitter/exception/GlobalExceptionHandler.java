package com.mwitter.exception;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.ExceptionHandler;//bir hata oluşursa bu metod çalışacak
import org.springframework.web.bind.annotation.RestControllerAdvice;//Bütün Controller'ları merkezi olarak yönet

@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(RuntimeException.class)//RuntimeException hatası oluşursa handleRuntimeException metodu çalışacak
public ErrorResponse handleRuntimeException(RuntimeException ex) {//ex değişkeni içinde fırlatılan hata var

    return new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage()
        );

}
}
