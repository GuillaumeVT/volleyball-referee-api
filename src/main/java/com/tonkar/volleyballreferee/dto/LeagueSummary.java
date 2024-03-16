package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class LeagueSummary {
    @NotNull
    private UUID     id;
    @NotBlank
    private String   createdBy;
    private long     createdAt;
    private long     updatedAt;
    @NotNull
    private GameType kind;
    @NotBlank
    private String   name;
}
