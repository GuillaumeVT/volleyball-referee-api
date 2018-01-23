package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameRepository extends MongoRepository<Game,String> {

    Game findGameByDate(long date);

    void deleteGameByDate(long date);

    void deleteGameByLiveAndDate(boolean live, long date);

    long deleteGamesByDateLessThan(long date);

    long deleteGamesByLiveAndDateLessThan(boolean live, long date);
}
