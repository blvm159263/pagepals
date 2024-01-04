package com.pagepal.capstone.repositories;

import com.pagepal.capstone.entities.postgre.Book;
import com.pagepal.capstone.enums.Status;
import com.pagepal.capstone.repositories.postgre.BookRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@DataJpaTest
public class BookRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    public void testFindByTitleContainingIgnoreCase() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle("test");
        book.setAuthor("test");
        book.setCreatedAt(null);
        book.setStatus(Status.ACTIVE);

        bookRepository.save(book);
        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase("", PageRequest.of(0, 10));
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    public void testFindByTitleContainsIgnoreCaseAndAuthor() {
        Page<Book> result = bookRepository.findByTitleContainsIgnoreCaseAndAuthor("", "", PageRequest.of(0, 10));
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}
