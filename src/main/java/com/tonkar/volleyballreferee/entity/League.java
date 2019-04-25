package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor @Getter @Setter
@Document(collection="leagues")
public class League {

    @Id
    @NotNull
    private UUID         id;
    @NotBlank
    private String       createdBy;
    private long         createdAt;
    private long         updatedAt;
    @NotNull
    private GameType     kind;
    @NotBlank
    private String       name;
    @NotNull
    private List<String> divisions;

}
