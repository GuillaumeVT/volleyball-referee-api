package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team,String> {

    Team findByNameAndUserIdAndGender(String name, String userId, String gender);

    List<Team> findByUserId(String userId);

    List<Team> findByUserIdAndKind(String userId, String kind);

    void deleteByNameAndUserIdAndGender(String name, String userId, String gender);

    long countByUserId(String userId);
}
