package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Statistics;

public interface StatisticsService {

    Statistics getStatistics();

    Statistics getStatistics(String userId);

}
