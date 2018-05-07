package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team,String> {

    Team findByNameAndUserId_SocialIdAndUserId_ProviderAndGender(String name, String socialId, String provider, String gender);

    List<Team> findByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    List<Team> findByUserId_SocialIdAndUserId_ProviderAndKind(String socialId, String provider, String kind);

    void deleteByNameAndUserId_SocialIdAndUserId_ProviderAndGender(String name, String socialId, String provider, String gender);

    long countByUserId_SocialIdAndUserId_Provider(String socialId, String provider);
}
