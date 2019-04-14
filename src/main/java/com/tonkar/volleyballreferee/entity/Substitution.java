package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class Substitution {

    private int playerIn;
    private int playerOut;
    private int homePoints;
    private int guestPoints;

}
