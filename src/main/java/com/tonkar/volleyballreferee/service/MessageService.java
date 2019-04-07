package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.Message;
import com.tonkar.volleyballreferee.exception.NotFoundException;

public interface MessageService {

    Message getMessage() throws NotFoundException;

}
