package com.b2g.lendservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
public class LendingPeriod {

    @Column(name = "start_date", nullable = false)
    private LocalDate start;

    @Column(name = "end_date", nullable = false)
    private LocalDate end;

    public LendingPeriod(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }
    public LendingPeriod(LocalDate start, LendingOption option) {
        this.start = start;
        this.end = end;
    }

    public int durationDays() {
        return (int) (end.toEpochDay() - start.toEpochDay());
    }
    public boolean isValid() {
        return start != null && end != null && !end.isBefore(start);
    }
}
