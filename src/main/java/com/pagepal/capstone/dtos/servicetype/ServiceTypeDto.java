package com.pagepal.capstone.dtos.servicetype;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeDto {
    private UUID id;

    private String name;

    private String description;

}
