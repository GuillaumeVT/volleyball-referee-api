package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.GameDescription;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GameDescriptionRepository extends MongoRepository<GameDescription,String> {

    boolean existsByDate(long date);

    boolean existsByDateAndUserId(long date, String userId);

    boolean existsByUserIdAndStatusAndRules(String userId, String status, String rulesName);

    @ExistsQuery("{ '$and': [ { 'userId': ?0 }, { 'status': ?1 }, { 'kind': ?2 }, { 'gender': ?3 }, { '$or': [ { 'hName': ?4 }, { 'gName': ?4 } ] } ] }")
    boolean existsByUserIdAndStatusAndKindAndGenderAndName(String userId, String status, String kind, String gender, String teamName);

    GameDescription findByDate(long date);

    GameDescription findByDateAndUserId(long date, String userId);

    List<GameDescription> findByStatusAndIndexedOrderByScheduleDesc(String status, boolean indexed);

    List<GameDescription> findByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLikeOrRefereeIgnoreCaseLikeAndIndexedOrderByScheduleDesc(String hName, String gName, String leagueName, String refereeName, boolean indexed);

    List<GameDescription> findByScheduleBetweenAndIndexedOrderByScheduleDesc(long fromDate, long toDate, boolean index);

    List<GameDescription> findByUserIdOrderByScheduleDesc(String userId);

    List<GameDescription> findByUserIdAndStatusOrderByScheduleDesc(String userId, String status);

    @Query("{ '$and': [ { 'userId': ?0 }, { '$or': [ { 'status': 'SCHEDULED' }, { 'status': 'LIVE' } ] } ] }")
    List<GameDescription> findByUserIdAndAvailable(String userId, Sort sort);

    List<GameDescription> findByUserIdAndKindAndLeague(String userId, String kind, String leagueName);

    List<GameDescription> findByUserIdAndStatusAndRules(String userId, String status, String rulesName);

    @Query("{ '$and': [ { 'userId': ?0 }, { 'status': ?1 }, { 'kind': ?2 }, { 'gender': ?3 }, { '$or': [ { 'hName': ?4 }, { 'gName': ?4 } ] } ] }")
    List<GameDescription> findByUserIdAndStatusAndKindAndGenderAndTeamName(String userId, String status, String kind, String gender, String teamName);

    @Query("{ '$and': [ { 'userId': ?0 }, { 'kind': ?1 }, { 'league': ?2 }, { '$or': [ { 'hName': ?3 }, { 'gName': ?3 } ] }, { 'gender': ?4 } ] }")
    List<GameDescription> findByUserIdAndKindAndLeagueAndTeamNameAndGender(String userId, String kind, String leagueName, String teamName, String teamGender);

    List<GameDescription> findTop10ByUserIdAndKindAndStatusAndLeagueOrderByScheduleAsc(String userId, String kind, String status, String leagueName);

    List<GameDescription> findTop10ByUserIdAndKindAndStatusAndLeagueOrderByScheduleDesc(String userId, String kind, String status, String leagueName);

    List<GameDescription> findByUserIdAndStatusAndLeague(String userId, String status, String leagueName);

    List<GameDescription> findByUserIdAndKindAndStatusAndLeagueOrderByScheduleDesc(String userId, String kind, String status, String leagueName);

    List<GameDescription> findByUserIdAndStatusAndLeagueAndDivision(String userId, String status, String leagueName, String divisionName);

    List<GameDescription> findByUserIdAndKindAndLeagueAndDivisionNot(String userId, String kind, String leagueName, String notDivisionName);

    List<GameDescription> findByUserIdAndKindAndLeagueNotAndDivisionNot(String userId, String kind, String notLeagueName, String notDivisionName);

    void deleteByDateAndUserId(long date, String userId);

    void deleteByDateAndUserIdAndStatus(long date, String userId, String status);

    long deleteByScheduleLessThanAndUserId(long schedule, String userId);

    long deleteByScheduleLessThanAndStatus(long schedule, String status);

    long deleteByScheduleLessThanAndUserIdAndStatus(long schedule, String userId, String status);

    long count();

    long countByStatus(String status);

    long countByUserId(String userId);

    long countByUserIdAndKindAndLeague(String userId, String kind, String leagueName);

}