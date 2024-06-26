package com.pagepal.capstone.dtos.customer;

import com.pagepal.capstone.entities.postgre.Account;
import com.pagepal.capstone.entities.postgre.Booking;
import com.pagepal.capstone.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReadDto {
    private String fullName;
    private GenderEnum gender;
    private String dob;
    private String imageUrl;
    private String createdAt;
    private Account account;
    private List<Booking> bookings;
}
