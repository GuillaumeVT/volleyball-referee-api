package com.tonkar.volleyballreferee.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor @AllArgsConstructor @Builder @Getter @Setter
@Document(collection="passwordResets")
public class PasswordReset {

    @Id
    @NotNull
    private UUID          id;
    @NotBlank
    private String        userId;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private LocalDateTime expiresAt;

}
