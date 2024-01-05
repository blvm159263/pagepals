package com.pagepal.capstone.dtos.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookQueryDto {
    private String search;
    private String sort;
    private String author;
    private UUID categoryId;
    private Integer page;
    private Integer pageSize;
}
