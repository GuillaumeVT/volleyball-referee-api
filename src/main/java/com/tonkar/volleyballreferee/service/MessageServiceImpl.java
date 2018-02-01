package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.Message;
import com.tonkar.volleyballreferee.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public boolean hasMessage() {
        return messageRepository.existsByHook(Message.MESSAGE_HOOK);
    }

    @Override
    public String getMessage() {
        String message = null;

        if (hasMessage()) {
            message = messageRepository.findOneByHook(Message.MESSAGE_HOOK).getContent();
        }

        return message;
    }

    @Override
    public void addMessage(String content) {
        if (!hasMessage()) {
            Message message = new Message();
            message.setContent(content);
            message.setHook(Message.MESSAGE_HOOK);
            messageRepository.save(message);
        }
    }

    @Override
    public void removeMessage() {
        messageRepository.deleteAll();
    }
}
