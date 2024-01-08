package com.pagepal.capstone.repositories.postgre;

import com.pagepal.capstone.entities.postgre.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
}
