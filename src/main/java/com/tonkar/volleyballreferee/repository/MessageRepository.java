package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {}
