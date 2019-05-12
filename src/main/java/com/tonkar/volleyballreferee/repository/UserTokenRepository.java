package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.UserToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserTokenRepository extends MongoRepository<UserToken, String> {

    Optional<UserToken> findByToken(String token);

}
