package com.pagepal.capstone.services.impl;

import com.pagepal.capstone.dtos.book.BookDto;
import com.pagepal.capstone.dtos.book.ListBookDto;
import com.pagepal.capstone.dtos.googlebook.GoogleBook;
import com.pagepal.capstone.dtos.pagination.PagingDto;
import com.pagepal.capstone.entities.postgre.Author;
import com.pagepal.capstone.entities.postgre.Book;
import com.pagepal.capstone.entities.postgre.Category;
import com.pagepal.capstone.enums.Status;
import com.pagepal.capstone.mappers.BookMapper;
import com.pagepal.capstone.repositories.AuthorRepository;
import com.pagepal.capstone.repositories.BookRepository;
import com.pagepal.capstone.repositories.CategoryRepository;
import com.pagepal.capstone.services.BookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    private final AuthorRepository authorRepository;

    @Override
    public ListBookDto getListBookForCustomer(String search, String sort, String author, UUID categoryId, Integer page, Integer pageSize) {
        if (page == null || page < 0)
            page = 0;

        if (pageSize == null || pageSize < 0)
            pageSize = 10;

        Pageable pageable;
        if (sort != null && sort.equals("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by("title").descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by("title").ascending());
        }

        if (search == null)
            search = "";
        List<Author> authors = null;
        if (!(author == null || author.isEmpty()))
            authors = authorRepository.findByNameContainingIgnoreCase(author);

        Page<Book> bookPage;
        if (categoryId == null)
            if (authors == null || authors.isEmpty())
                bookPage = bookRepository.findByTitleContainingIgnoreCase(search, pageable);
            else
                bookPage = bookRepository.searchBooksByAuthorAndTitle(authors, search, pageable);
        else {
            Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found"));
            bookPage = bookRepository.searchBooksByAuthorCategoryAndTitle(authors, List.of(category), search, pageable);
        }

        ListBookDto listBookDto = new ListBookDto();
        if (bookPage == null) {
            listBookDto.setList(Collections.emptyList());
            listBookDto.setPagination(null);
            return listBookDto;
        } else {
            PagingDto pagingDto = new PagingDto();
            pagingDto.setTotalOfPages(bookPage.getTotalPages());
            pagingDto.setTotalOfElements(bookPage.getTotalElements());
            pagingDto.setSort(bookPage.getSort().toString());
            pagingDto.setCurrentPage(bookPage.getNumber());
            pagingDto.setPageSize(bookPage.getSize());

            listBookDto.setList(bookPage.map(BookMapper.INSTANCE::toDto).toList());
            listBookDto.setPagination(pagingDto);
            return listBookDto;
        }
    }

    @Override
    public BookDto getBookById(UUID id) {
        return bookRepository.findById(id)
                .map(BookMapper.INSTANCE::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
    }

    @Override
    public Book createNewBook(GoogleBook book) {
        Book newBook = new Book();
        List<Category> categories = new ArrayList<>();
        List<Author> authors = new ArrayList<>();
        for (String category : book.getVolumeInfo().getCategories()) {
            Category newCategory = categoryRepository.findByName(category).orElse(null);
            if (newCategory == null) {
                newCategory = new Category();
                newCategory.setName(category);
                newCategory.setStatus(Status.ACTIVE);
                newCategory = categoryRepository.save(newCategory);
            }
            categories.add(newCategory);
        }
        for (String author : book.getVolumeInfo().getAuthors()) {
            Author newAuthor = authorRepository.findByName(author).orElse(null);
            if (newAuthor == null) {
                newAuthor = new Author();
                newAuthor.setName(author);
                newAuthor.setStatus(Status.ACTIVE);
                newAuthor = authorRepository.save(newAuthor);
            }
            authors.add(newAuthor);
        }
        var bookExtId = book.getId();
        if (bookExtId == null) bookExtId = UUID.randomUUID().toString();
        else if (bookExtId.isEmpty() || bookExtId.isBlank()) {
            bookExtId = UUID.randomUUID().toString();
        }

        newBook.setAuthors(authors);
        newBook.setCategories(categories);
        newBook.setExternalId(bookExtId);
        newBook.setTitle(book.getVolumeInfo().getTitle());
        newBook.setPublisher(book.getVolumeInfo().getPublisher());
        newBook.setPublishedDate(book.getVolumeInfo().getPublishedDate());
        newBook.setDescription(book.getVolumeInfo().getDescription());
        newBook.setPageCount(book.getVolumeInfo().getPageCount());
        newBook.setThumbnailUrl(book.getVolumeInfo().getImageLinks().getThumbnail());
        newBook.setLanguage(book.getVolumeInfo().getLanguage());
        newBook.setSmallThumbnailUrl(book.getVolumeInfo().getImageLinks().getSmallThumbnail());
        newBook.setStatus(Status.ACTIVE);
        return bookRepository.save(newBook);
    }
}
