package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.Rules;
import com.tonkar.volleyballreferee.model.Team;
import com.tonkar.volleyballreferee.model.User;
import com.tonkar.volleyballreferee.model.UserId;

import java.util.List;

public interface UserService {

    User getUser(UserId userId);

    List<Rules> getUserRules(User user);

    List<Rules> getDefaultRules();

    Rules getUserRules(User user, String name);

    long getNumberOfUserRules(User user);

    boolean createUserRules(User user, Rules rules);

    boolean updateUserRules(User user, Rules rules);

    boolean deleteUserRules(User user, String name);

    List<Team> getUserTeams(User user);

    Team getUserTeam(User user, String name);

    long getNumberOfUserTeams(User user);

    boolean createUserTeam(User user, Team team);

    boolean updateUserTeam(User user, Team team);

    boolean deleteUserTeam(User user, String name);
}
