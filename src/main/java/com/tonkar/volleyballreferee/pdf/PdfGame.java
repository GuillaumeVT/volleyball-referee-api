package com.tonkar.volleyballreferee.pdf;

public class PdfGame {

    private String filename;
    private byte[] data;

    public PdfGame(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
