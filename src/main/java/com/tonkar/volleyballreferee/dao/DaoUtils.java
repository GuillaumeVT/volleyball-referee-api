package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;

import java.util.List;

public class DaoUtils {

    public static final String _id = "_id";

    public static List<GameStatus> computeStatuses(List<GameStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(GameStatus.values());
        }
        return statuses;
    }

    public static List<GameType> computeKinds(List<GameType> kinds) {
        if (kinds == null || kinds.isEmpty()) {
            kinds = List.of(GameType.values());
        }
        return kinds;
    }

    public static List<GenderType> computeGenders(List<GenderType> genders) {
        if (genders == null || genders.isEmpty()) {
            genders = List.of(GenderType.values());
        }
        return genders;
    }
}
