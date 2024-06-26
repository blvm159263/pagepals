package com.pagepal.capstone.mappers;

import com.pagepal.capstone.dtos.service.ServiceCustomerDto;
import com.pagepal.capstone.dtos.service.ServiceDto;
import com.pagepal.capstone.dtos.servicetype.ServiceTypeDto;
import com.pagepal.capstone.entities.postgre.Reader;
import com.pagepal.capstone.entities.postgre.Service;
import com.pagepal.capstone.entities.postgre.ServiceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toDateFormat")
    ServiceDto toDto(Service book);

    @Named("readerIdToReader")
    default Reader readerIdToReader(UUID id) {
        Reader reader = new Reader();
        reader.setId(id);
        return reader;
    }

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toDateFormat")
    ServiceCustomerDto toCustomerDto(Service book);

    Service toEntity(ServiceDto serviceDto);

    ServiceTypeDto toServiceTypeDto(ServiceType serviceType);

    @Named("toDateFormat")
    default String toDateFormat(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return outputFormat.format(date);
    }
}
