package com.pagepal.capstone.services;

import com.pagepal.capstone.dtos.book.BookDto;
import com.pagepal.capstone.entities.postgre.Book;
import com.pagepal.capstone.enums.Status;
import com.pagepal.capstone.repositories.postgre.BookRepository;
import com.pagepal.capstone.services.impl.BookServiceImpl;
import graphql.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BookServiceImpl.class)
public class BookServiceTest {
    @MockBean
    private BookRepository bookRepository;

    @Autowired
    private BookServiceImpl bookService;

    @Before
    public void setUp() {
        Book book1 = new Book();
        book1.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        book1.setTitle("test1");
        book1.setAuthor("test1");
        book1.setCreatedAt(null);
        book1.setStatus(Status.ACTIVE);

        Book book2 = new Book();
        book2.setId(UUID.randomUUID());
        book2.setTitle("test2");
        book2.setAuthor("test2");
        book2.setCreatedAt(null);
        book2.setStatus(Status.ACTIVE);

        Book book3 = new Book();
        book3.setId(UUID.randomUUID());
        book3.setTitle("test3");
        book3.setAuthor("test4");
        book3.setCreatedAt(null);
        book3.setStatus(Status.ACTIVE);

        Mockito.when(bookRepository.saveAll(Mockito.anyList())).thenReturn(List.of(book1, book2, book3));
    }


    @Test
    public void testGetListBook() {

        Book book1 = new Book();
        book1.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        book1.setTitle("test1");
        book1.setAuthor("test1");
        book1.setCreatedAt(null);
        book1.setStatus(Status.ACTIVE);

        Book book2 = new Book();
        book2.setId(UUID.randomUUID());
        book2.setTitle("test2");
        book2.setAuthor("test2");
        book2.setCreatedAt(null);
        book2.setStatus(Status.ACTIVE);

        Book book3 = new Book();
        book3.setId(UUID.randomUUID());
        book3.setTitle("test3");
        book3.setAuthor("test4");
        book3.setCreatedAt(null);
        book3.setStatus(Status.ACTIVE);

        when(bookRepository.findByTitleContainingIgnoreCase(eq(""), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book1, book2, book3)));

        List<BookDto> result = bookService.getListBook("", "", "", 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
    }

}
