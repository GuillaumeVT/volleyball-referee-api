package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository extends MongoRepository<League, UUID> {

    List<League> findByCreatedByAndKindOrderByNameAsc(String userId, GameType kind);

    List<League> findByCreatedByOrderByNameAsc(String userId);

    Optional<League> findByIdAndCreatedBy(UUID id, String userId);

    boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind);

    long countByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);

}
