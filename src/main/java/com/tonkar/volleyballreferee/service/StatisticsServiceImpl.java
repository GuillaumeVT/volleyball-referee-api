package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.StatisticsDao;
import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsDao statisticsDao;

    @Override
    public Statistics getStatistics() {
        List<Statistics.Count> gameStatistics = statisticsDao.findGameStatistics();
        List<Statistics.Count> teamStatistics = statisticsDao.findTeamStatistics();

        Statistics statistics = new Statistics();
        statistics.getGameStatistics().addAll(gameStatistics);
        statistics.getTeamStatistics().addAll(teamStatistics);
        return statistics;
    }

    @Override
    public Statistics getStatistics(User user) {
        List<Statistics.Count> gameStatistics = statisticsDao.findGameStatistics(user.getId());
        List<Statistics.Count> teamStatistics = statisticsDao.findTeamStatistics(user.getId());

        Statistics statistics = new Statistics();
        statistics.getGameStatistics().addAll(gameStatistics);
        statistics.getTeamStatistics().addAll(teamStatistics);
        return statistics;
    }

}
