package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotBlank
    private String senderId;
    @NotBlank
    private String receiverId;
    @NotBlank
    private String senderPseudo;
    @NotBlank
    private String receiverPseudo;

}
