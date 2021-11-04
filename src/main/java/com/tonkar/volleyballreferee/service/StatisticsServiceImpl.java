package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.StatisticsDao;
import com.tonkar.volleyballreferee.dto.StatisticsGroup;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsDao statisticsDao;

    @Override
    public StatisticsGroup getGlobalStatistics() {
        return statisticsDao.findGlobalStatistics();
    }

    @Override
    public StatisticsGroup getUserStatistics(User user) {
        return statisticsDao.findUserStatistics(user.getId());
    }

}
