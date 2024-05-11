package com.pagepal.capstone.dtos.service;

import com.pagepal.capstone.entities.postgre.Book;
import com.pagepal.capstone.entities.postgre.Booking;
import com.pagepal.capstone.entities.postgre.Reader;
import com.pagepal.capstone.entities.postgre.ServiceType;
import com.pagepal.capstone.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {
    private UUID id;
    private Integer price;
    private String createdAt;
    private String description;
    private Float duration;
    private Integer totalOfReview;
    private Integer totalOfBooking;
    private Integer rating;
    private String imageUrl;
    private Status status;
    private Reader reader;
    private Book book;
    private ServiceType serviceType;
    private List<Booking> bookings;
}
