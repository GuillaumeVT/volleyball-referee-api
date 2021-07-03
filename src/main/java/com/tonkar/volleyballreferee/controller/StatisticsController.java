package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping(value = "/statistics", produces = {"application/json"})
    public ResponseEntity<Statistics> getStatistics(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(statisticsService.getStatistics(user), HttpStatus.OK);
    }
}
