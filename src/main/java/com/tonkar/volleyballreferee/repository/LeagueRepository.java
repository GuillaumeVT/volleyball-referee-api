package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.League;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LeagueRepository extends MongoRepository<League,String> {

    League findLeagueByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    List<League> findLeaguesByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    List<League> findLeaguesByUserId_SocialIdAndUserId_ProviderAndKind(String socialId, String provider, String kind);

    void deleteLeagueByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    long countLeaguesByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

}
