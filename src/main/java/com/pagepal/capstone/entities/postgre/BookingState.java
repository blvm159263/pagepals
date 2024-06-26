package com.pagepal.capstone.entities.postgre;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pagepal.capstone.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BOOKING_STATE")
@Where(clause = "status = 'ACTIVE'")
public class BookingState implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "state", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Booking> bookings;
}
