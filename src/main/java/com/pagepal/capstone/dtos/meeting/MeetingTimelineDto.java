package com.pagepal.capstone.dtos.meeting;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pagepal.capstone.entities.postgre.Meeting;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
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

    private MeetingDto meeting;
}