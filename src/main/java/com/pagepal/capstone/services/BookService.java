package com.pagepal.capstone.services;

import com.pagepal.capstone.dtos.book.BookDto;

import java.util.List;

public interface BookService {
    List<BookDto> getListBook(String search, String sort, String filter, Integer page, Integer pageSize);
}
