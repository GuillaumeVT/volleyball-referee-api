package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class LeagueDao {

    private final static ProjectionOperation sLeagueSummaryProjection = Aggregation.project()
            .and(_id).as(_id)
            .and(League.Fields.createdBy).as(LeagueSummary.Fields.createdBy)
            .and(League.Fields.createdAt).as(LeagueSummary.Fields.createdAt)
            .and(League.Fields.updatedAt).as(LeagueSummary.Fields.updatedAt)
            .and(League.Fields.name).as(LeagueSummary.Fields.name)
            .and(League.Fields.kind).as(LeagueSummary.Fields.kind);

    private final MongoTemplate mongoTemplate;

    public List<LeagueSummary> listLeagues(String userId, List<GameType> kinds) {
        kinds = DaoUtils.computeKinds(kinds);

        MatchOperation matchOperation = Aggregation.match(Criteria.where(League.Fields.createdBy).is(userId).and(League.Fields.kind).in(kinds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, LeagueSummary.Fields.name);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sLeagueSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(League.class), LeagueSummary.class).getMappedResults();
    }

    public List<LeagueSummary> listLeaguesOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(League.Fields.createdBy).is(userId).and(League.Fields.kind).is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, LeagueSummary.Fields.name);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sLeagueSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(League.class), LeagueSummary.class).getMappedResults();
    }

    public void save(League league) {
        mongoTemplate.save(league);
    }

    public Optional<League> findById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, League.class));
    }

    public Optional<League> findByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(League.Fields.createdBy).is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, League.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, League.class);
    }

    public boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind) {
        Query query = Query.query(Criteria.where(League.Fields.createdBy).is(userId).and(League.Fields.name).is(name).and(League.Fields.kind).is(kind));
        return mongoTemplate.exists(query, League.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where(League.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, League.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(League.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, League.class);
    }
}
