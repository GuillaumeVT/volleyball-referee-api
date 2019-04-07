package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document(collection="messages")
@NoArgsConstructor @Getter @Setter
public class Message {

    @Id
    @NotBlank
    private String id;
    @NotBlank
    private String content;

}
