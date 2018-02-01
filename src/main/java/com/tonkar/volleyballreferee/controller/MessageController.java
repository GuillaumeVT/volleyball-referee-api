package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
@CrossOrigin("*")
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @GetMapping("")
    public ResponseEntity<String> getMessage() {
        LOGGER.debug("Request get message");
        return new ResponseEntity<>(messageService.getMessage(), HttpStatus.OK);
    }

    @GetMapping("/has")
    public ResponseEntity<Boolean> hasMessage() {
        LOGGER.debug("Request has message");
        return new ResponseEntity<>(messageService.hasMessage(), HttpStatus.OK);
    }

}