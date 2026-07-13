package com.mwitter.dto;//bu sınıfın görevi backendden frontende cevap taşımak.
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data//sınıftaki alanlar için otomatik get set üretir
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
private LocalDateTime timestamp;
    private String message;

}
