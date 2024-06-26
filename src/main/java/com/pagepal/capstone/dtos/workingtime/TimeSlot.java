package com.pagepal.capstone.dtos.workingtime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    private UUID id;
    private String startTime;
    private String endTime;
    private Boolean isSeminar;
    private Boolean isBooked;
}
