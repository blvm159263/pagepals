package com.pagepal.capstone.mappers;

import com.pagepal.capstone.dtos.book.BookDto;
import com.pagepal.capstone.entities.postgre.Book;
import com.pagepal.capstone.entities.postgre.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    @Mapping(target = "category", source = "category")
    BookDto toDto(Book book);

    @Mapping(target = "category", source = "category.id", qualifiedByName = "toEntityWithCategory")
    Book toEntity(BookDto bookDto);

    @Named("toEntityWithCategory")
    default Category toEntityWithCategory(UUID id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
