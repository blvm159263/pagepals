package com.pagepal.capstone.controllers;

import com.pagepal.capstone.dtos.book.BookDto;
import com.pagepal.capstone.dtos.book.BookQueryDto;
import com.pagepal.capstone.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @QueryMapping(name = "getListBook")
    public List<BookDto> getListBook(@Argument(name = "query")BookQueryDto query) {
        return bookService.getListBook(
                query.getSearch(),
                query.getSort(),
                query.getFilter(),
                query.getPage(),
                query.getPageSize());
    }

//    @Argument(name = "search") Optional<String> search,
//    @Argument(name = "sort") Optional<String> sort,
//    @Argument(name = "filter") Optional<String> filter,
//    @Argument(name = "page") Optional<Integer> page,
//    @Argument(name = "pageSize") Optional<Integer> pageSize
}
