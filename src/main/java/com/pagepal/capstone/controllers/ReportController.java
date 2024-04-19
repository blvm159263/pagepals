package com.pagepal.capstone.controllers;

import com.pagepal.capstone.dtos.report.ListReportDto;
import com.pagepal.capstone.dtos.report.ReportCreateDto;
import com.pagepal.capstone.dtos.report.ReportQueryDto;
import com.pagepal.capstone.dtos.report.ReportReadDto;
import com.pagepal.capstone.dtos.seminar.ReportBookingDto;
import com.pagepal.capstone.dtos.seminar.ReportPostDto;
import com.pagepal.capstone.dtos.seminar.ReportReaderDto;
import com.pagepal.capstone.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @QueryMapping
    public ReportReadDto getReportById(@Argument("id") UUID id) {
        return reportService.getReportById(id);
    }

    @QueryMapping
    public ListReportDto listReport(@Argument("query") ReportQueryDto query){
        return reportService.getAllReports(query);
    }

    @MutationMapping
    public ReportReadDto createReport(@Argument("input") ReportCreateDto report){
        return reportService.createReport(report);
    }

    @QueryMapping
    public List<ReportBookingDto> listReportBooking(){
        return reportService.listReportBooking();
    }

    @QueryMapping
    public List<ReportReaderDto> listReportReader(){
        return reportService.listReportReader();
    }

    @QueryMapping
    public List<ReportPostDto> listReportPost(){
        return reportService.listReportPost();
    }
}