package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends MongoRepository<Game, UUID> {

    List<Game> findByIdAndCreatedByAndLeagueIdAndStatusOrderByScheduledAtAsc(String userId, UUID leagueId, GameStatus status);

    List<Game> findByIdAndCreatedByAndLeagueIdAndStatusAndDivisionNameOrderByScheduledAtAsc(String userId, UUID leagueId, GameStatus status, String divisionName);

    Optional<Game> findByIdAndCreatedBy(UUID id, String userId);

    @Query("{ '$and': [ { 'id': ?0 }, { '$or': [ { 'createdBy': ?1 }, { 'refereedBy': ?1 } ] } , { 'status': ?2 } ] }")
    Optional<Game> findByIdAndAllowedUserAndStatus(UUID id, String userId, GameStatus status);

    @Query("{ '$and': [ { 'id': ?0 }, { '$or': [ { 'createdBy': ?1 }, { 'refereedBy': ?1 } ] } , { 'status': { $ne: ?2 } } ] }")
    Optional<Game> findByIdAndAllowedUserAndStatusNot(UUID id, String userId, GameStatus status);

    boolean existsByCreatedByAndRules_IdAndStatus(String userId, UUID rulesId, GameStatus status);

    @ExistsQuery("{ '$and': [ { 'createdBy': ?0 }, { 'status': ?2 }, { '$or': [ { 'homeTeam.id': ?1 }, { 'guestTeam.id': ?1 } ] } ] }")
    boolean existsByCreatedByAndTeamAndStatus(String userId, UUID teamId, GameStatus status);

    boolean existsByCreatedByAndLeagueIdAndStatus(String userId, UUID leagueId, GameStatus status);

    long countByCreatedBy(String userId);

    long countByCreatedByAndLeagueId(String userId, UUID leagueId);

    void deleteByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);

    void deleteByScheduledAtLessThanAndStatus(long scheduledAt, GameStatus status);

}
