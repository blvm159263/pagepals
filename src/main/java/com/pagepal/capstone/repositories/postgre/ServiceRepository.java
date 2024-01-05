package com.pagepal.capstone.repositories.postgre;

import com.pagepal.capstone.entities.postgre.Reader;
import com.pagepal.capstone.entities.postgre.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    List<Service> findAllByReader(Reader reader);
}
