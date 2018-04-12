package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {

    User findUserByUserId_SocialIdAndUserId_Provider(String id, String provider);

}
