package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Rules;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RulesRepository extends MongoRepository<Rules,String> {

    Rules findByNameAndUserId(String name, String userId);

    List<Rules> findByUserIdOrderByNameAsc(String userId);

    void deleteByUserId(String userId);

    void deleteByNameAndUserId(String name, String userId);

    long countByUserId(String userId);
}
