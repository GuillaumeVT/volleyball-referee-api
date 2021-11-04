package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.StatisticsGroup;
import com.tonkar.volleyballreferee.entity.User;

public interface StatisticsService {

    StatisticsGroup getGlobalStatistics();

    StatisticsGroup getUserStatistics(User user);

}
