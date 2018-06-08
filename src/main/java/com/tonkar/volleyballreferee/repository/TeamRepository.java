package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team,String> {

    Team findByNameAndUserIdAndGenderAndKind(String name, String userId, String gender, String kind);

    List<Team> findByUserId(String userId);

    List<Team> findByUserIdAndKind(String userId, String kind);

    void deleteByUserId(String userId);

    void deleteByNameAndUserIdAndGenderAndKind(String name, String userId, String gender, String kind);

    long countByUserId(String userId);
}
