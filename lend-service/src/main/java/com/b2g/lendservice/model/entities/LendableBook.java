package com.b2g.lendservice.model.entities;

import com.b2g.commons.FormatType;
import com.b2g.commons.SubscriptionType;
import com.b2g.lendservice.Exceptions.LendingOptionException;
import com.b2g.lendservice.model.vo.LendingOption;
import com.b2g.lendservice.model.vo.UserSubscriptionData;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "lendable_books")
@Getter
@NoArgsConstructor
public class LendableBook {

    @Id
    private UUID formatId;

    @Column(nullable = false)
    private UUID bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatType type;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lendable_book_id")
    private Set<LendingOption> options = new HashSet<>();

    public LendableBook(UUID bookId, UUID formatId, FormatType type) {
        this.bookId = bookId;
        this.formatId = formatId;
        this.type = type;
    }

    public boolean isAvailableForUser(UserSubscriptionData userSubscription) {
        if (options == null || options.isEmpty()) {
            return false; // nessuna opzione ⇒ non disponibile
        }

        return options.stream()
                .filter(opt -> opt.isApplicableFor(userSubscription))
                .max(Comparator.comparing(LendingOption::getMinRequiredTierName))
                .isPresent();
    }
    public void addOption(LendingOption option) {
        boolean alreadyExists = options.stream()
                .anyMatch(o -> o.getMinRequiredTier().equals(option.getMinRequiredTier()));

        if (alreadyExists) {
            throw new LendingOptionException(
                    "LendingOption with minRequiredTier " + option.getMinRequiredTier() + " already exists for this lendable"
            );
        }

        this.options.add(option);
    }
    public void removeOption(UUID optionId) {
        this.options.removeIf(o -> o.getId().equals(optionId));
    }

    public Optional<LendingOption> findOption(UUID optionId) {
        return options.stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst();
    }

    public boolean needsToBeRetrievedAtLibrary() {
        return type==FormatType.PHYSICAL;

    }

    public boolean hasOption(UUID optionId) {
        return findOption(optionId).isPresent();
    }

    public LendingOption getMaxApplicableOption(SubscriptionType tier) {
        return options.stream()
                .filter(opt -> tier.isAtLeast(opt.getMinRequiredTier()))  // tier >= option.minRequired
                .max(Comparator.comparing(o -> o.getMinRequiredTier().getLevel()))
                .orElse(null);
    }
}
