package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="messages")
public class Message {

    public static int MESSAGE_HOOK = 1;

    @Id
    private String id;
    private String content;
    private int    hook;

    public Message() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHook() {
        return hook;
    }

    public void setHook(int hook) {
        this.hook = hook;
    }
}
