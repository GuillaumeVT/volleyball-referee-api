package com.tonkar.volleyballreferee.scoresheet;

public class ScoreSheet {

    private final String filename;
    private final byte[] data;

    ScoreSheet(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

}
