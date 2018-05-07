package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.League;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LeagueRepository extends MongoRepository<League,String> {

    League findByDate(long date);

    League findByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    League findByNameAndUserId_SocialIdAndUserId_Provider(String name, String socialId, String provider);

    List<League> findByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    List<League> findByUserId_SocialIdAndUserId_ProviderAndKind(String socialId, String provider, String kind);

    void deleteByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long countByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

}
