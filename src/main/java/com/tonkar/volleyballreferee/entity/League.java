package com.tonkar.volleyballreferee.entity;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
@Document(collection = "leagues")
public class League {

    @Id
    @NotNull
    private UUID         id;
    @NotNull
    private UUID         createdBy;
    private long         createdAt;
    private long         updatedAt;
    @NotNull
    private GameType     kind;
    @NotBlank
    private String       name;
    @NotNull
    private List<String> divisions;

}
