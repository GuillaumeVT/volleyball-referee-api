package com.tonkar.volleyballreferee.entity;

import com.tonkar.volleyballreferee.dto.LeagueDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor @Getter @Setter
public class SelectedLeague extends LeagueDescription {

    @NotBlank
    private String division;

}
