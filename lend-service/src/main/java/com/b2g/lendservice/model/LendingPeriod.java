package com.b2g.lendservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
public class LendingPeriod {

    @Column(name = "start_date", nullable = true)
    private LocalDate start;

    @Column(name = "end_date", nullable = true)
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
    @JsonIgnore
    public boolean isValid() {
        return start != null && end != null && !end.isBefore(start);
    }
}
