package com.pagepal.capstone.entities.postgre;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pagepal.capstone.enums.EventStateEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EVENT")
@Where(clause = "state = 'ACTIVE'")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "start_at")
    private Date startAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "limit_customer")
    private Integer limitCustomer;

    @Column(name = "price")
    private Integer price;

    @Column(name = "active_slot")
    private Integer activeSlot;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private EventStateEnum state;

    @ManyToOne
    @JoinColumn(name = "seminar_id")
    @JsonManagedReference
    private Seminar seminar;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Booking> bookings;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<BannerAds> bannerAds;

    @OneToOne
    @JoinColumn(name = "meeting_id")
    @JsonManagedReference
    private Meeting meeting;
}
