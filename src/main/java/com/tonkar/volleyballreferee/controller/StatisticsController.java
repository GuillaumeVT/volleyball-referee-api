package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.StatisticsGroupDto;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatisticsGroupDto> getUserStatistics(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(statisticsService.getUserStatistics(user), HttpStatus.OK);
    }
}
