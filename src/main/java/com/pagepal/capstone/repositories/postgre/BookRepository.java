package com.pagepal.capstone.repositories.postgre;

import com.pagepal.capstone.entities.postgre.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    // get list book by title
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // get list book by title and filter by author
    Page<Book> findByTitleContainsIgnoreCaseAndAuthor(String title, String author, Pageable pageable);
}
