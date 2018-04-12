package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team,String> {

    Team findTeamByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    List<Team> findTeamsByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    void deleteTeamByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    long countTeamsByUserId_SocialIdAndUserId_Provider(String socialId, String provider);
}
