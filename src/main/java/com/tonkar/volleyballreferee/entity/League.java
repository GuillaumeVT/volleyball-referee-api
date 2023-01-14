package com.tonkar.volleyballreferee.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
@Document(collection = "leagues")
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
