package com.pagepal.capstone.entities.postgre;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pagepal.capstone.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SERVICE")
@Where(clause = "status = 'ACTIVE'")
public class Service implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "price")
    private Integer price;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "total_of_review")
    private Integer totalOfReview;

    @Column(name = "total_of_booking")
    private Integer totalOfBooking;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "reader_id")
    @JsonManagedReference
    private Reader reader;

    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonManagedReference
    private Book book;

    @ManyToOne
    @JoinColumn(name = "service_type_id")
    @JsonManagedReference
    private ServiceType serviceType;

    @OneToMany(mappedBy = "service")
    @JsonBackReference
    private List<Booking> bookings;
}
