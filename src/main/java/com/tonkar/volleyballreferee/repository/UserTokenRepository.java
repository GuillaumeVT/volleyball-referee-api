package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.UserToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserTokenRepository extends MongoRepository<UserToken, String> {}
