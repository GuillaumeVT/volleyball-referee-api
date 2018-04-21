package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;

import java.util.List;

public interface UserService {

    List<Rules> getUserRules(UserId userId);

    List<Rules> getDefaultRules();

    Rules getUserRules(UserId userId, String name);

    long getNumberOfUserRules(UserId userId);

    boolean createUserRules(Rules rules);

    boolean updateUserRules(Rules rules);

    boolean deleteUserRules(UserId userId, String name);

    List<Team> getUserTeams(UserId userId);

    Team getUserTeam(UserId userId, String name);

    long getNumberOfUserTeams(UserId userId);

    boolean createUserTeam(Team team);

    boolean updateUserTeam(Team team);

    boolean deleteUserTeam(UserId userId, String name);

    List<GameDescription> getUserGames(UserId userId);

    Game getUserGame(UserId userId, long date);

    long getNumberOfUserGames(UserId userId);

    boolean createUserGame(GameDescription gameDescription);

    boolean updateUserGame(GameDescription gameDescription);

    boolean deleteUserGame(UserId userId, long date);
}
