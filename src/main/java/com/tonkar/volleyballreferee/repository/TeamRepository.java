package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.entity.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface TeamRepository extends MongoRepository<Team, UUID> {

    Stream<Team> findByCreatedByOrderByNameAsc(String userId);

    Optional<Team> findByIdAndCreatedBy(UUID id, String userId);

    Optional<Team> findByIdAndCreatedByAndKind(UUID id, String userId, GameType kind);

    boolean existsByCreatedByAndNameAndKindAndGender(String userId, String name, GameType kind, GenderType gender);

    long countByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);

    void deleteByCreatedBy(String userId);
}
