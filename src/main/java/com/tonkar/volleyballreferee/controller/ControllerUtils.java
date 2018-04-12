package com.tonkar.volleyballreferee.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ControllerUtils {

    public static String decodeUrlParameters(String value) {
        String newValue;

        try {
            newValue = URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            newValue = value;
        }

        return newValue;
    }

}
