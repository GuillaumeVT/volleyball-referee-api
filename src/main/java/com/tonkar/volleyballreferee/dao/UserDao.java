package com.tonkar.volleyballreferee.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final static ProjectionOperation sUserSummaryProjection = Aggregation
            .project()
            .and(_id)
            .as(_id)
            .and(User.Fields.pseudo)
            .as(UserSummaryDto.Fields.pseudo)
            .and(User.Fields.admin)
            .as(UserSummaryDto.Fields.admin);

    private final MongoTemplate mongoTemplate;

    public void save(User user) {
        mongoTemplate.save(user);
    }

    public void delete(User user) {
        mongoTemplate.remove(user);
    }

    public boolean existsByPseudo(String pseudo) {
        Query query = Query.query(Criteria.where(User.Fields.pseudo).is(pseudo));
        return mongoTemplate.exists(query, User.class);
    }

    public Optional<User> findById(UUID id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public Optional<User> findByPseudo(String pseudo) {
        Query query = Query.query(Criteria.where(User.Fields.pseudo).is(pseudo));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public boolean areFriends(UUID id1, UUID id2) {
        Query query = Query.query(Criteria.where("id").is(id1).and(User.Fields.friends + "." + User.Friend.Fields.id).is(id2));
        return mongoTemplate.exists(query, User.class);
    }

    public boolean updateUserSignedIn(UUID userId, long lastLoginAt) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update()
                .set(User.Fields.lastLoginAt, lastLoginAt)
                .set(User.Fields.failedAuthentication + "." + User.FailedAuthentication.Fields.attempts, 0);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean addFailedAuthentication(UUID userId, User.FailedAuthentication failedAuthentication) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().set(User.Fields.failedAuthentication, failedAuthentication);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPassword(UUID userId, String password) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update()
                .set(User.Fields.password, password)
                .set(User.Fields.failedAuthentication + "." + User.FailedAuthentication.Fields.attempts, 0);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPseudo(UUID userId, String pseudo) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().set(User.Fields.pseudo, pseudo);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateFriendPseudo(UUID userId, String pseudo) {
        Query query = new Query(Criteria.where(User.Fields.friends + "." + User.Friend.Fields.id).is(userId));
        Update update = new Update().set(User.Fields.friends + ".$." + User.Friend.Fields.pseudo, pseudo);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean addFriend(UUID userId, User.Friend friend) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().addToSet(User.Fields.friends, friend);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean removeFriend(UUID userId, UUID friendId) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().pull(User.Fields.friends, new BasicDBObject(_id, friendId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public Page<UserSummaryDto> listUsers(String filter, Pageable pageable) {
        Criteria criteria;

        if (StringUtils.isNotBlank(filter)) {
            criteria = Criteria
                    .where(User.Fields.admin)
                    .is(false)
                    .orOperator(Criteria.where(User.Fields.pseudo).regex(".*" + filter + ".*", "i"));
        } else {
            criteria = Criteria.where(User.Fields.admin).is(false);
        }

        long total = mongoTemplate.count(Query.query(criteria), User.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, UserSummaryDto.Fields.pseudo);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());

        List<UserSummaryDto> users = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sUserSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(User.class), UserSummaryDto.class)
                .getMappedResults();

        return new PageDto<>(users, pageable, total);
    }
}
