package com.pagepal.capstone.services;

import com.pagepal.capstone.dtos.service.ServiceDto;

import java.util.List;
import java.util.UUID;

public interface ServiceProvideService {
    List<ServiceDto> getAllServicesByReaderId(UUID readerId);
}
