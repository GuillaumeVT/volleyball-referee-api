package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.configuration.CacheConfiguration;
import com.tonkar.volleyballreferee.dao.StatisticsDao;
import com.tonkar.volleyballreferee.dto.StatisticsGroup;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsDao statisticsDao;

    @Cacheable(cacheNames = CacheConfiguration.GLOBAL_STATISTICS_CACHE, key = "#root.methodName")
    public StatisticsGroup getGlobalStatistics() {
        return statisticsDao.findGlobalStatistics();
    }

    @Cacheable(cacheNames = CacheConfiguration.GLOBAL_STATISTICS_CACHE, key = "#user.id")
    public StatisticsGroup getUserStatistics(User user) {
        return statisticsDao.findUserStatistics(user.getId());
    }

}
