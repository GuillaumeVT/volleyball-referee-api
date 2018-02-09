package com.tonkar.volleyballreferee.model;

public class PenaltyCard {

    private String card;
    private int    num;
    private int    set;
    private int    hPoints;
    private int    gPoints;

    public PenaltyCard() {}

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
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
