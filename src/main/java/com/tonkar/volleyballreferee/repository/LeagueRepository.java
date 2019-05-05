package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository extends MongoRepository<League, UUID> {

    Optional<League> findByIdAndCreatedBy(UUID id, String userId);

    boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind);

    long countByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);

}
