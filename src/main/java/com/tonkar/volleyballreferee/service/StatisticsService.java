package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.entity.User;

public interface StatisticsService {

    Statistics getStatistics();

    Statistics getStatistics(User user);

}
