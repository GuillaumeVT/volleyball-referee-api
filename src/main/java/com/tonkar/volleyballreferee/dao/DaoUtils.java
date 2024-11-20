package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.entity.*;

import java.util.Set;

public class DaoUtils {

    public static final String _id = "_id";

    public static Set<GameStatus> computeStatuses(Set<GameStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = Set.of(GameStatus.values());
        }
        return statuses;
    }

    public static Set<GameType> computeKinds(Set<GameType> kinds) {
        if (kinds == null || kinds.isEmpty()) {
            kinds = Set.of(GameType.values());
        }
        return kinds;
    }

    public static Set<GenderType> computeGenders(Set<GenderType> genders) {
        if (genders == null || genders.isEmpty()) {
            genders = Set.of(GenderType.values());
        }
        return genders;
    }
}
