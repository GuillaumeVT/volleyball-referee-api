package com.tonkar.volleyballreferee.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class Court {

    private int p1;
    private int p2;
    private int p3;
    private int p4;
    private int p5;
    private int p6;

    public boolean isFilled() {
        return p1 > 0 && p2 > 0 && p3 > 0 && p4 > 0 && p5 > 0 && p6 > 0;
    }
}
