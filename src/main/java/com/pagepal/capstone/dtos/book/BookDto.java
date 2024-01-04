package com.pagepal.capstone.dtos.book;

import com.pagepal.capstone.dtos.category.CategoryDto;
import com.pagepal.capstone.entities.postgre.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private UUID id;
    private String title;
    private String longTitle;
    private String author;
    private String publisher;
    private Long pages;
    private String language;
    private String overview;
    private String imageUrl;
    private String edition;
    private Date createdAt;
    private CategoryDto category;
}
