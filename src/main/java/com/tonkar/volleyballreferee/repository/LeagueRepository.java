package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.League;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LeagueRepository extends MongoRepository<League,String> {

    League findByDate(long date);

    League findByDateAndUserId(long date, String userId);

    League findByNameAndUserId(String name, String userId);

    List<League> findByUserIdOrderByNameAsc(String userId);

    List<League> findByUserIdAndKindOrderByNameAsc(String userId, String kind);

    void deleteByDateAndUserId(long date, String userId);

    long countByUserId(String userId);

}
