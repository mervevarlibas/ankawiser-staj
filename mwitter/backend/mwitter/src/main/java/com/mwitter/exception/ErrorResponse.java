package com.mwitter.exception;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data //Lombok  getter ve setter'ları oluşturacak
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;//hata oluştuğu zamanı tutuyoruz

    private String message;//hata mesajını tutuyoruz
  private List<String> errors;
}
