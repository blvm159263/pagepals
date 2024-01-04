package com.pagepal.capstone.dtos.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookQueryDto {
    private String search;
    private String sort;
    private String filter;
    private Integer page;
    private Integer pageSize;
}
