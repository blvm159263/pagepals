package com.pagepal.capstone.dtos.reader;

import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReaderQueryDto {
    private String nickname;
    private Integer rating;
    private String genre;
    private String language;
    private String countryAccent;
    private String sort;
    private Integer page;
    private Integer pageSize;
}
