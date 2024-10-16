package com.tonkar.volleyballreferee.entity;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
@Document(collection = "friendRequests")
public class FriendRequest {

    @Id
    @NotNull
    private UUID   id;
    @NotNull
    private UUID   senderId;
    @NotNull
    private UUID   receiverId;
    @NotBlank
    private String senderPseudo;
    @NotBlank
    private String receiverPseudo;

}
