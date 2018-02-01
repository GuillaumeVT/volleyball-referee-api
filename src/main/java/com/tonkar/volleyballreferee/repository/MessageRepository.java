package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message,String> {

    boolean existsByHook(int hook);

    Message findOneByHook(int hook);

}
