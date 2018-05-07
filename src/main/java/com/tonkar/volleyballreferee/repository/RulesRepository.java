package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Rules;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RulesRepository extends MongoRepository<Rules,String> {

    Rules findByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    List<Rules> findByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    void deleteByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    long countByUserId_SocialIdAndUserId_Provider(String socialId, String provider);
}
