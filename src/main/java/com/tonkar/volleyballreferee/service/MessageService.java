package com.tonkar.volleyballreferee.service;

public interface MessageService {

    boolean hasMessage();

    String getMessage();

    void addMessage(String content);

    void removeMessage();

}
