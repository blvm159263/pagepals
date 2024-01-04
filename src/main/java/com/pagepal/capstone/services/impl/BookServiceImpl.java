package com.pagepal.capstone.services.impl;

import com.pagepal.capstone.dtos.book.BookDto;
import com.pagepal.capstone.dtos.book.WriteBookDto;
import com.pagepal.capstone.entities.postgre.Book;
import com.pagepal.capstone.enums.Status;
import com.pagepal.capstone.mappers.BookMapper;
import com.pagepal.capstone.repositories.postgre.BookRepository;
import com.pagepal.capstone.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
//    private final BookMapper bookMapper;

    @Override
    public List<BookDto> getListBook(String search, String sort, String filter, Integer page, Integer pageSize) {
        if (page == null || page < 0)
            page = 0;

        if (pageSize == null || pageSize < 0)
            pageSize = 10;

        Pageable pageable;
        if (sort != null && sort.equals("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").ascending());
        }

        if(search == null)
            search = "";

        if (filter == null || filter.isEmpty())
            return bookRepository.findByTitleContainingIgnoreCase(search, pageable).map(BookMapper.INSTANCE::toDto).toList();
        else
            return bookRepository.findByTitleContainsIgnoreCaseAndAuthor(search, filter, pageable).map(BookMapper.INSTANCE::toDto).toList();
    }

    @Override
    public BookDto createBook(WriteBookDto bookDto) {
        Book book = BookMapper.INSTANCE.createBook(bookDto);
        book.setId(null);
        book.setStatus(Status.ACTIVE);
        return BookMapper.INSTANCE.toDto(bookRepository.save(book));
    }
}
