package com.tonkar.volleyballreferee.model;

public class Substitution {

    private int pIn;
    private int pOut;
    private int hPoints;
    private int gPoints;

    public Substitution() {}

    public int getpIn() {
        return pIn;
    }

    public void setpIn(int pIn) {
        this.pIn = pIn;
    }

    public int getpOut() {
        return pOut;
    }

    public void setpOut(int pOut) {
        this.pOut = pOut;
    }

    public int gethPoints() {
        return hPoints;
    }

    public void sethPoints(int hPoints) {
        this.hPoints = hPoints;
    }

    public int getgPoints() {
        return gPoints;
    }

    public void setgPoints(int gPoints) {
        this.gPoints = gPoints;
    }
}
