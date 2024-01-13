package com.pagepal.capstone.dtos.booking;

import com.pagepal.capstone.entities.postgre.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private UUID id;
    private Double totalPrice;
    private String promotionCode;
    private String description;
    private String review;
    private Integer rating;
    private Date createAt;
    private Date updateAt;
    private Date deleteAt;
    private Date startAt;
    private Customer customer;
    private Meeting meeting;
    private BookingState state;
    private List<BookingDetail> bookingDetails;
    private List<Transaction> transactions;
}