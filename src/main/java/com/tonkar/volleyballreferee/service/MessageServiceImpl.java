package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.Message;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public Message getMessage() throws NotFoundException {
        String mainMessageId = "main-message";
        return messageRepository
                .findById(mainMessageId)
                .orElseThrow(() -> new NotFoundException(""));
    }
}