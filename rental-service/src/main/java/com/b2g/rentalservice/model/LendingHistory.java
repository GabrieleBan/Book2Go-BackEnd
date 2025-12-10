package com.b2g.rentalservice.model;

import com.b2g.commons.FormatType;
import com.b2g.commons.LendState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LendingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private UUID bookId;

    @Enumerated(EnumType.STRING)
    private FormatType type;
    @Column(nullable = false)

    private UUID formatId;
    @Column(nullable = true)
    private Integer physBookId;
//    @ManyToOne
//    @JoinColumn(name = "rental_option_id")
//    private RentalOption rentalOption;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LendState state;
}
