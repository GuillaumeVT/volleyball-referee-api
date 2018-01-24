package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.GameDescription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameDescriptionRepository extends MongoRepository<GameDescription,String> {

    boolean existsByDate(long date);

    GameDescription findGameDescriptionByDate(long date);

    List<GameDescription> findGameDescriptionsByLive(boolean live);

    List<GameDescription> findGameDescriptionsByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLike(String hName, String gName, String league);

    List<GameDescription> findGameDescriptionsByDateBetween(long fromDate, long toDate);

    void deleteGameDescriptionByDate(long date);

    void deleteGameDescriptionByLiveAndDate(boolean live, long date);

    long deleteGameDescriptionsByDateLessThan(long date);

    long deleteGameDescriptionsByLiveAndDateLessThan(boolean live, long date);

    long count();

    long countByLive(boolean live);


}