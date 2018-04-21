package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameRepository extends MongoRepository<Game,String> {

    Game findGameByDate(long date);

    Game findGameByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    void deleteGameByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    void deleteGameByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteGamesByDateLessThanAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteGamesByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    List<Game> findGamesByUserId_SocialIdAndUserId_ProviderAndStatusAndSets_DurationLessThan(String socialId, String provider, String status, long setDurationMillisUnder);
}
