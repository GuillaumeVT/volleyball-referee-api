package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameRepository extends MongoRepository<Game,String> {

    Game findByDate(long date);

    Game findByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    List<Game> findByUserId_SocialIdAndUserId_ProviderAndStatusAndSets_DurationLessThan(String socialId, String provider, String status, long setDurationMillisUnder);

    void deleteByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    void deleteByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteByDateLessThanAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

}
