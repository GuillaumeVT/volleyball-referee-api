package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/statistics")
@CrossOrigin("*")
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<Statistics> getStatistics(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(statisticsService.getStatistics(user.getId()), HttpStatus.OK);
    }
}
