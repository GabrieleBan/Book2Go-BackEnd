package com.b2g.lendservice.model.entities;

import com.b2g.commons.LendState;
import com.b2g.commons.SubscriptionType;
import com.b2g.lendservice.Exceptions.LendingStateLifeCycleException;
import com.b2g.lendservice.model.vo.LendingOption;
import com.b2g.lendservice.model.vo.LendingPeriod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "lendings")
@Getter
@NoArgsConstructor
public class Lending {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private SubscriptionType subscriptionType;
    @Column(nullable = false)
    private UUID lendingOptionId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lendableBookId", column = @Column(name = "lendable_book_id", nullable = false)),
            @AttributeOverride(name = "copyNumber", column = @Column(name = "copy_number"))
    })
    private LendableCopy copy;
    @Column(nullable = false)
    private LocalDate requestedAt;
    @Embedded
    private LendingPeriod period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LendState state;
    @Column(nullable = true)
    private UUID libraryId;

    protected Lending(@NotNull UUID userId,@NotNull LendableCopy copy,@NotNull UUID lendingOptionId, UUID libraryId, LendState state,SubscriptionType subscriptionType) {
        if(copy.isPhysical() && libraryId==null) {
            throw new IllegalArgumentException("libraryId cannot be null if copy is physical");
        }
        this.userId = userId;
        this.copy = copy;
        this.state = state;
        this.lendingOptionId = lendingOptionId;
        this.libraryId = libraryId;
        this.requestedAt = LocalDate.now();
        this.period = new LendingPeriod();
        this.subscriptionType = subscriptionType;
    }


    public static Lending create(UUID userId, UUID lendingOptionId,UUID libraryId, LendableCopy copy, SubscriptionType subscriptionType) {
        return new Lending(userId, copy,lendingOptionId, libraryId, LendState.PROCESSING,subscriptionType);
    }

    public void startLending(LendingOption usedOption, LendableCopy copy) {

        this.copy = copy;

        switch (this.state) {

            case AWAITING:
                if (!copy.isPhysical()) {
                    throw new LendingStateLifeCycleException(
                            "Only physical copies can move from AWAITING to LENDING"
                    );
                }
                this.state = LendState.LENDING;
                break;

            case PROCESSING:
                if (copy.isPhysical()) {
                    throw new LendingStateLifeCycleException(
                            "Physical copy cannot go directly from PROCESSING to LENDING"
                    );
                }
                this.state = LendState.LENDING;
                break;

            default:
                throw new LendingStateLifeCycleException("Cannot start lending from " + state);
        }

        LocalDate today = LocalDate.now();
        this.period = new LendingPeriod(today, today.plusDays(usedOption.getDurationDays()));
    }

//    public void markAsProcessing() {
//        this.state = LendState.PROCESSING;
//    }

    public void markAsAwaiting() {
        this.state = LendState.AWAITING;
    }

    public void conclude() {
        this.state = LendState.CONCLUDED;
    }

    public void fail() {
        this.state = LendState.FAILED;
    }

    public void addCopy(LendableCopy copy) {
        if(state != LendState.PROCESSING && state != LendState.AWAITING) {
            throw new LendingStateLifeCycleException("Only PROCESSING lends can move from PROCESSING to AWAITING");
        }
        if(copy.isPhysical() && libraryId==null) {
            throw new IllegalArgumentException("libraryId cannot be null if copy is physical");
        }
        this.copy = copy;
        this.state = LendState.AWAITING;
        this.period = new LendingPeriod(LocalDate.now(), (LocalDate) null);

    }
}