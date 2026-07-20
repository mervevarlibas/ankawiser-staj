package com.mwitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//frontendin profile yönlendirme yapabilmesi için hem username hem userid gönderliyor
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentionResponse {
    private String userId;
    private String username;
}
