package com.tonkar.volleyballreferee.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection="users")
public class User {

    @Id
    private String       id;
    private UserId       userId;
    private Set<String>  rules;
    private Set<String>  teams;
    private Set<String>  competitions;

    public User() {
        rules = new HashSet<>();
        teams = new HashSet<>();
        competitions = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public Set<String> getRules() {
        return rules;
    }

    public void setRules(Set<String> rules) {
        this.rules = rules;
    }

    public Set<String> getTeams() {
        return teams;
    }

    public void setTeams(Set<String> teams) {
        this.teams = teams;
    }

    public Set<String> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(Set<String> competitions) {
        this.competitions = competitions;
    }
}
