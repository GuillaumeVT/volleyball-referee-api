package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import com.tonkar.volleyballreferee.entity.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends MongoRepository<Team, UUID> {

    List<Team> findByCreatedByOrderByNameAsc(String userId);

    Optional<Team> findByIdAndCreatedBy(UUID id, String userId);

    Optional<Team> findByIdAndCreatedByAndKindAndGender(UUID id, String userId, GameType kind, GenderType gender);

    boolean existsByCreatedByAndNameAndKindAndGender(String userId, String name, GameType kind, GenderType gender);

    long countByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);
}
