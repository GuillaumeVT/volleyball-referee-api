package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Rules;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RulesRepository extends MongoRepository<Rules,String> {

    Rules findRulesByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    List<Rules> findRulesByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    void deleteRulesByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    long countRulesByUserId_SocialIdAndUserId_Provider(String socialId, String provider);
}
