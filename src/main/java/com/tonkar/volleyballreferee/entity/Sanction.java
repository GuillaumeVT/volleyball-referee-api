package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor @Getter @Setter
public class Sanction {

    @NotBlank
    private String card;
    private int    num;
    private int    set;
    private int    hPoints;
    private int    gPoints;

}
