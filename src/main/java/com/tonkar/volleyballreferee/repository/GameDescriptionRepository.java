package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.GameDescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GameDescriptionRepository extends MongoRepository<GameDescription,String> {

    boolean existsByDate(long date);

    boolean existsByDateAndUserId(long date, String userId);

    boolean existsByUserIdAndStatusAndRules(String userId, String status, String rulesName);

    boolean existsByUserIdAndStatusAndHName(String userId, String status, String teamName);

    boolean existsByUserIdAndStatusAndGName(String userId, String status, String teamName);

    GameDescription findByDate(long date);

    GameDescription findByDateAndUserId(long date, String userId);

    List<GameDescription> findByStatusAndIndexed(String status, boolean indexed);

    List<GameDescription> findByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLikeOrRefereeIgnoreCaseLikeAndIndexed(String hName, String gName, String leagueName, String refereeName, boolean indexed);

    List<GameDescription> findByScheduleBetweenAndIndexed(long fromDate, long toDate, boolean index);

    List<GameDescription> findByUserId(String userId);

    List<GameDescription> findByUserIdAndStatus(String userId, String status);

    @Query("{ '$and': [ { 'userId': ?0 }, { '$or': [ { 'status': 'SCHEDULED' }, { 'status': 'LIVE' } ] } ] }")
    List<GameDescription> findByUserIdAndAvailable(String userId);

    List<GameDescription> findByUserIdAndKindAndLeague(String userId, String kind, String leagueName);

    List<GameDescription> findByUserIdAndStatusAndRules(String userId, String status, String rulesName);

    @Query("{ '$and': [ { 'userId': ?0 }, { 'status': ?1 }, { '$or': [ { 'hName': ?2 }, { 'gName': ?2 } ] } ] }")
    List<GameDescription> findByUserIdAndStatusAndTeamName(String userId, String status, String teamName);

    @Query("{ '$and': [ { 'userId': ?0 }, { 'kind': ?1 }, { 'league': ?2 }, { '$or': [ { 'hName': ?3 }, { 'gName': ?3 } ] }, { 'gender': ?4 } ] }")
    List<GameDescription> findByUserIdAndKindAndLeagueAndTeamNameAndGender(String userId, String kind, String leagueName, String teamName, String teamGender);

    List<GameDescription> findTop10ByUserIdAndKindAndStatusAndLeagueOrderByScheduleAsc(String userId, String kind, String status, String leagueName);

    List<GameDescription> findTop10ByUserIdAndKindAndStatusAndLeagueOrderByScheduleDesc(String userId, String kind, String status, String leagueName);

    List<GameDescription> findByUserIdAndStatusAndLeague(String userId, String status, String leagueName);

    List<GameDescription> findByUserIdAndKindAndStatusAndLeague(String userId, String kind, String status, String leagueName);

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