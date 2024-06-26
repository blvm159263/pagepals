package com.pagepal.capstone.dtos.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingTimelineDto {
    private UUID id;

    private String userName;

    private String action;

    private String time;

}
