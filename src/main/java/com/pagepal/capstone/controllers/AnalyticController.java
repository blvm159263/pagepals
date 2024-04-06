package com.pagepal.capstone.controllers;

import com.pagepal.capstone.dtos.analytic.AnalyticAdmin;
import com.pagepal.capstone.services.AnalyticService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AnalyticController {
    private final AnalyticService analyticService;

    @QueryMapping
    public AnalyticAdmin getAnalyticAdmin(@Argument("startDate") String startDate,
                                          @Argument("endDate") String endDate) {
        return analyticService.getAnalyticAdminByDate(startDate, endDate);
    }
}
