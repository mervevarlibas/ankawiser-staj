package com.mwitter.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {//burada yalnızca content var userid jwtden alınacak,postid urlden alınacak
@NotBlank(message = "Comment content cannot be empty")
    @Size(
        max = 200,
        message = "Comment content cannot be longer than 200 characters"
    )
    private String content;
}
