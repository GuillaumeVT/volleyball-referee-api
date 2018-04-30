package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.GameDescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GameDescriptionRepository extends MongoRepository<GameDescription,String> {

    boolean existsByDate(long date);

    boolean existsByDateAndStatus(long date, String status);

    boolean existsByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(String socialId, String provider, String status, String rulesName);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndHName(String socialId, String provider, String status, String teamName);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndGName(String socialId, String provider, String status, String teamName);

    GameDescription findGameDescriptionByDate(long date);

    GameDescription findGameDescriptionByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    List<GameDescription> findGameDescriptionsByStatus(String status);

    List<GameDescription> findGameDescriptionsByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLikeOrRefereeIgnoreCaseLike(String hName, String gName, String leagueName, String refereeName);

    List<GameDescription> findGameDescriptionsByDateBetween(long fromDate, long toDate);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndKindAndLeague(String socialId, String provider, String kind, String leagueName);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(String socialId, String provider, String status, String rulesName);

    @Query("{ '$and': [ { 'userId.socialId': ?0 }, { 'userId.provider': ?1 }, { 'status': ?2 }, { '$or': [ { 'hName': ?3 }, { 'gName': ?3 } ] } ] }")
    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndStatusAndTeamName(String socialId, String provider, String status, String teamName);

    @Query("{ '$and': [ { 'userId.socialId': ?0 }, { 'userId.provider': ?1 }, { 'kind': ?2 }, { 'league': ?3 }, { '$or': [ { 'hName': ?4 }, { 'gName': ?4 } ] }, { 'gender': ?5 } ] }")
    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndKindAndLeagueAndTeamNameAndGender(String socialId, String provider, String kind, String leagueName, String teamName, String teamGender);

    void deleteGameDescriptionByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    void deleteGameDescriptionByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    long deleteGameDescriptionsByDateLessThanAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteGameDescriptionsByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    long count();

    long countByStatus(String status);

    long countByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

}