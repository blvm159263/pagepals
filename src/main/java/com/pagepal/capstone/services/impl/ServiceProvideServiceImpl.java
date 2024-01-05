package com.pagepal.capstone.services.impl;

import com.pagepal.capstone.dtos.service.QueryDto;
import com.pagepal.capstone.dtos.service.ServiceDto;
import com.pagepal.capstone.mappers.ServiceMapper;
import com.pagepal.capstone.repositories.postgre.BookRepository;
import com.pagepal.capstone.repositories.postgre.ReaderRepository;
import com.pagepal.capstone.repositories.postgre.ServiceRepository;
import com.pagepal.capstone.services.ServiceProvideService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceProvideServiceImpl implements ServiceProvideService {

    private final ServiceRepository serviceRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    @Override
    public List<ServiceDto> getAllServicesByReaderId(UUID readerId, QueryDto queryDto) {
        if(queryDto.getPage() == null || queryDto.getPage() < 0)
            queryDto.setPage(0);

        if(queryDto.getPageSize() == null || queryDto.getPageSize() < 0)
            queryDto.setPageSize(10);

        Pageable pageable;
        if(queryDto.getSort() != null && queryDto.getSort().equalsIgnoreCase("desc")){
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("createdAt").descending());
        }
        else{
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("createdAt").ascending());
        }

        if(queryDto.getSearch() == null){
            queryDto.setSearch("");
        }

        var reader = readerRepository.findById(readerId).orElseThrow(
                () -> new RuntimeException("Reader not found")
        );

        var services = serviceRepository.findAllByReaderAndBookTitleContainsIgnoreCase(reader, queryDto.getSearch(), pageable);
        return services.stream().map(ServiceMapper.INSTANCE::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ServiceDto> getAllServicesByBookId(UUID bookId, QueryDto queryDto) {
        if(queryDto.getPage() == null || queryDto.getPage() < 0)
            queryDto.setPage(0);

        if(queryDto.getPageSize() == null || queryDto.getPageSize() < 0)
            queryDto.setPageSize(10);

        Pageable pageable;
        if(queryDto.getSort() != null && queryDto.getSort().equalsIgnoreCase("desc")){
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("createdAt").descending());
        }
        else{
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("createdAt").ascending());
        }

        if(queryDto.getSearch() == null){
            queryDto.setSearch("");
        }

        var book = bookRepository.findById(bookId).orElseThrow(
                () -> new RuntimeException("Book not found")
        );

        var services = serviceRepository.findAllByBookId(book, queryDto.getSearch(), pageable);
        return services.stream().map(ServiceMapper.INSTANCE::toDto).collect(Collectors.toList());
    }
}