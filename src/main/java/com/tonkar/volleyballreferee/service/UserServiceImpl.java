package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.Rules;
import com.tonkar.volleyballreferee.model.Team;
import com.tonkar.volleyballreferee.model.User;
import com.tonkar.volleyballreferee.model.UserId;
import com.tonkar.volleyballreferee.repository.RulesRepository;
import com.tonkar.volleyballreferee.repository.TeamRepository;
import com.tonkar.volleyballreferee.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository  userRepository;

    @Autowired
    private RulesRepository rulesRepository;

    @Autowired
    private TeamRepository  teamRepository;

    @Override
    public User getUser(UserId userId) {
        User user = userRepository.findUserByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());

        if (user == null) {
            user = new User();
            user.setUserId(userId);
            userRepository.insert(user);
            LOGGER.info(String.format("Created user %s", userId));
        }

        return user;
    }

    @Override
    public List<Rules> getUserRules(User user) {
        return rulesRepository.findRulesByUserId_SocialIdAndUserId_Provider(user.getUserId().getSocialId(), user.getUserId().getProvider());
    }

    @Override
    public List<Rules> getDefaultRules() {
        List<Rules> rules = new ArrayList<>();
        rules.add(Rules.OFFICIAL_INDOOR_RULES);
        rules.add(Rules.OFFICIAL_BEACH_RULES);
        rules.add(Rules.DEFAULT_INDOOR_4X4_RULES);
        return rules;
    }

    @Override
    public Rules getUserRules(User user, String name) {
        return rulesRepository.findRulesByNameAndUserId_SocialIdAndUserId_Provider(name, user.getUserId().getSocialId(), user.getUserId().getProvider());
    }

    @Override
    public long getNumberOfUserRules(User user) {
        return rulesRepository.countRulesByUserId_SocialIdAndUserId_Provider(user.getUserId().getSocialId(), user.getUserId().getProvider());
    }

    @Override
    public boolean createUserRules(User user, Rules rules) {
        final boolean created;

        if (getUserRules(user, rules.getName()) == null) {
            rulesRepository.insert(rules);
            LOGGER.info(String.format("Created rules %s for user %s", rules.getName(), user.getUserId()));
            user.getRules().add(rules.getName());
            userRepository.save(user);
            LOGGER.info(String.format("Added rules %s to user %s", rules.getName(), user.getUserId()));
            created = true;
        } else {
            LOGGER.error(String.format("Could not create rules %s for user %s because they already exist", rules.getName(), user.getUserId()));
            created = false;
        }

        return created;
    }

    @Override
    public boolean updateUserRules(User user, Rules rules) {
        final boolean updated;

        Rules savedRules = getUserRules(user, rules.getName());

        if (savedRules == null) {
            LOGGER.error(String.format("Could not update rules %s for user %s because they don't exist", rules.getName(), user.getUserId()));
            updated = false;
        } else {
            savedRules.setDate(rules.getDate());
            savedRules.setSetsPerGame(rules.getSetsPerGame());
            savedRules.setPointsPerSet(rules.getPointsPerSet());
            savedRules.setTieBreakInLastSet(rules.isTieBreakInLastSet());
            savedRules.setTwoPointsDifference(rules.isTwoPointsDifference());
            savedRules.setSanctions(rules.isSanctions());
            savedRules.setTeamTimeouts(rules.isTeamTimeouts());
            savedRules.setTeamTimeoutsPerSet(rules.getTeamTimeoutsPerSet());
            savedRules.setTeamTimeoutDuration(rules.getTeamTimeoutDuration());
            savedRules.setTechnicalTimeouts(rules.isTechnicalTimeouts());
            savedRules.setTechnicalTimeoutDuration(rules.getTechnicalTimeoutDuration());
            savedRules.setGameIntervals(rules.isGameIntervals());
            savedRules.setGameIntervalDuration(rules.getGameIntervalDuration());
            savedRules.setTeamSubstitutionsPerSet(rules.getTeamSubstitutionsPerSet());
            savedRules.setChangeSidesEvery7Points(rules.isChangeSidesEvery7Points());
            savedRules.setCustomConsecutiveServesPerPlayer(rules.getCustomConsecutiveServesPerPlayer());
            rulesRepository.save(savedRules);
            LOGGER.info(String.format("Updated rules %s for user %s", rules.getName(), user.getUserId()));
            updated = true;
        }

        return updated;
    }

    @Override
    public boolean deleteUserRules(User user, String name) {
        final boolean deleted;
        // TODO if a game uses these rules, return false
        //LOGGER.info(String.format("Could not delete rules %s for user %s because they are used in game %s", name, user.getUserId(), ));
        deleted = true;

        rulesRepository.deleteRulesByNameAndUserId_SocialIdAndUserId_Provider(name, user.getUserId().getSocialId(), user.getUserId().getProvider());
        LOGGER.info(String.format("Deleted rules %s for user %s", name, user.getUserId()));
        user.getRules().remove(name);
        userRepository.save(user);
        LOGGER.info(String.format("Deleted rules %s from user %s", name, user.getUserId()));

        return deleted;
    }

    @Override
    public List<Team> getUserTeams(User user) {
        return teamRepository.findTeamsByUserId_SocialIdAndUserId_Provider(user.getUserId().getSocialId(), user.getUserId().getProvider());
    }

    @Override
    public Team getUserTeam(User user, String name) {
        return teamRepository.findTeamByNameAndUserId_SocialIdAndUserId_Provider(name, user.getUserId().getSocialId(), user.getUserId().getProvider());
    }

    @Override
    public long getNumberOfUserTeams(User user) {
        return teamRepository.countTeamsByUserId_SocialIdAndUserId_Provider(user.getUserId().getSocialId(), user.getUserId().getProvider());
    }

    @Override
    public boolean createUserTeam(User user, Team team) {
        final boolean created;

        if (getUserTeam(user, team.getName()) == null) {
            teamRepository.insert(team);
            LOGGER.info(String.format("Created team %s for user %s", team.getName(), user.getUserId()));
            user.getTeams().add(team.getName());
            userRepository.save(user);
            LOGGER.info(String.format("Added team %s to user %s", team.getName(), user.getUserId()));
            created = true;
        } else {
            LOGGER.error(String.format("Could not create team %s for user %s because it already exists", team.getName(), user.getUserId()));
            created = false;
        }

        return created;
    }

    @Override
    public boolean updateUserTeam(User user, Team team) {
        final boolean updated;

        Team savedTeam = getUserTeam(user, team.getName());

        if (savedTeam == null) {
            LOGGER.error(String.format("Could not update team %s for user %s because it does not exist", team.getName(), user.getUserId()));
            updated = false;
        } else {
            savedTeam.setDate(team.getDate());
            savedTeam.setColor(team.getColor());
            savedTeam.setLiberoColor(team.getLiberoColor());
            savedTeam.setPlayers(team.getPlayers());
            savedTeam.setLiberos(team.getLiberos());
            savedTeam.setCaptain(team.getCaptain());
            savedTeam.setGender(team.getGender());
            teamRepository.save(savedTeam);
            LOGGER.info(String.format("Updated team %s for user %s", team.getName(), user.getUserId()));
            updated = true;
        }

        return updated;
    }

    @Override
    public boolean deleteUserTeam(User user, String name) {
        final boolean deleted;
        // TODO if a game uses this team, return false
        //LOGGER.info(String.format("Could not delete team %s for user %s because it is used in game %s", name, user.getUserId(), ));
        deleted = true;

        teamRepository.deleteTeamByNameAndUserId_SocialIdAndUserId_Provider(name, user.getUserId().getSocialId(), user.getUserId().getProvider());
        LOGGER.info(String.format("Deleted team %s for user %s", name, user.getUserId()));
        user.getTeams().remove(name);
        userRepository.save(user);
        LOGGER.info(String.format("Deleted team %s from user %s", name, user.getUserId()));

        return deleted;
    }
}
