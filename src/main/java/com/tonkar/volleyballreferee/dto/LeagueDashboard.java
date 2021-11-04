package com.tonkar.volleyballreferee.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LeagueDashboard {
    private List<GameSummary> liveGames;
    private List<GameSummary> last10Games;
    private List<GameSummary> next10Games;

    public LeagueDashboard() {
        this.liveGames = new ArrayList<>();
        this.last10Games = new ArrayList<>();
        this.next10Games = new ArrayList<>();
    }
}
