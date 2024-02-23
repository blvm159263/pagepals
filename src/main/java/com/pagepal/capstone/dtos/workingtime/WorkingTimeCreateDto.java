package com.pagepal.capstone.dtos.workingtime;

import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkingTimeCreateDto {
    private String date;
    private String startTime;
    private String endTime;
    private UUID readerId;
}
