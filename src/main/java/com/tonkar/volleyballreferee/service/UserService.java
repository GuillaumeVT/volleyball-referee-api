package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;

import java.util.List;

public interface UserService {

    List<Rules> getUserRules(UserId userId);

    List<Rules> getDefaultRules();

    Rules getUserRules(UserId userId, String name);

    Rules getRules(UserId userId, String name);

    long getNumberOfUserRules(UserId userId);

    boolean createUserRules(Rules rules);

    boolean updateUserRules(Rules rules);

    boolean deleteUserRules(UserId userId, String name);

    List<Team> getUserTeams(UserId userId);

    List<Team> getUserTeams(UserId userId, String kind);

    List<Team> getUserTeams(UserId userId, String kind, String leagueName);

    Team getUserTeam(UserId userId, String name, String gender);

    long getNumberOfUserTeams(UserId userId);

    boolean createUserTeam(Team team);

    boolean updateUserTeam(Team team);

    boolean deleteUserTeam(UserId userId, String name, String gender);

    List<GameDescription> getUserGames(UserId userId);

    List<GameDescription> getUserGames(UserId userId, String kind, String leagueName);

    List<GameDescription> getUserGames(UserId userId, String kind, String leagueName, String teamName);

    GameDescription getUserGame(UserId userId, long date);

    long getNumberOfUserGames(UserId userId);

    boolean createUserGame(GameDescription gameDescription);

    boolean updateUserGame(GameDescription gameDescription);

    boolean deleteUserGame(UserId userId, long date);

    int getUserGameCode(UserId userId, long date);

    List<League> getUserLeagues(UserId userId);

    List<League> getUserLeagues(UserId userId, String kind);

    League getUserLeague(UserId userId, String name);

    long getNumberOfUserLeagues(UserId userId);

    boolean createUserLeague(League league);

    boolean updateUserLeague(League league);

    boolean deleteUserLeague(UserId userId, String name);
}
