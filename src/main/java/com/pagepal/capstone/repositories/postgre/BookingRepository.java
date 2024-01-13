package com.pagepal.capstone.repositories.postgre;

import com.pagepal.capstone.entities.postgre.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.meeting.reader.id = :readerId
            AND b.meeting.reader.status = 'ACTIVE'
            """)
    List<Booking> findAllByReaderId(UUID readerId);

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.customer.id = :customerId
            AND b.customer.account.accountState.name = 'ACTIVE'
            """)
    Page<Booking> findAllByCustomerId(UUID customerId, Pageable pageable);
}