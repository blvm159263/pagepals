package com.pagepal.capstone.dtos.service;

import com.pagepal.capstone.entities.postgre.BookingDetail;
import com.pagepal.capstone.entities.postgre.Chapter;
import com.pagepal.capstone.entities.postgre.Reader;
import com.pagepal.capstone.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {
    private UUID id;
    private Float price;
    private Date createdAt;
    private String description;
    private Float duration;
    private Integer totalOfReview;
    private Integer totalOfBooking;
    private Integer rating;
    private Status status;
    private List<BookingDetail> bookingDetails;
    private Reader reader;
    private Chapter chapter;
}
