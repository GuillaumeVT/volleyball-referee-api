package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameRepository extends MongoRepository<Game,String> {

    Game findByDate(long date);

    Game findByDateAndUserId(long date, String userId);

    List<Game> findByUserIdAndStatusAndSets_DurationLessThan(String userId, String status, long setDurationMillisUnder);

    void deleteByDateAndUserId(long date, String userId);

    void deleteByDateAndUserIdAndStatus(long date, String userId, String status);

    long deleteByScheduleLessThanAndUserId(long schedule, String userId);

    long deleteByScheduleLessThanAndStatus(long schedule, String status);

    long deleteByScheduleLessThanAndUserIdAndStatus(long schedule, String userId, String status);

}
