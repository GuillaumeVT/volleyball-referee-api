package com.tonkar.volleyballreferee.model;

public class GameStatistics {

    private long gamesCount;
    private long liveGamesCount;

    public GameStatistics() {}

    public long getGamesCount() {
        return gamesCount;
    }

    public void setGamesCount(long gamesCount) {
        this.gamesCount = gamesCount;
    }

    public long getLiveGamesCount() {
        return liveGamesCount;
    }

    public void setLiveGamesCount(long liveGamesCount) {
        this.liveGamesCount = liveGamesCount;
    }
}
