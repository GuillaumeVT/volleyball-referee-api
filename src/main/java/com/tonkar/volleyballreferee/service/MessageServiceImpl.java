package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.Message;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public Message getMessage() throws NotFoundException {
        String mainMessageId = "main-message";
        Optional<Message> optMessage = messageRepository.findById(mainMessageId);

        if (optMessage.isPresent()) {
            return optMessage.get();
        } else {
            throw new NotFoundException(String.format("Could not find message %s", mainMessageId));
        }
    }
}