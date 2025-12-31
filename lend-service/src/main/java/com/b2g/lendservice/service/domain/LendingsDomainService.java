package com.b2g.lendservice.service.domain;

import com.b2g.lendservice.dto.LendingRequest;
import com.b2g.lendservice.model.entities.LendableBook;
import com.b2g.lendservice.model.entities.Lending;
import com.b2g.lendservice.model.entities.LendableCopy;
import com.b2g.lendservice.model.vo.LendingOption;
import com.b2g.lendservice.model.vo.LendingPeriod;
import com.b2g.lendservice.model.vo.UserSubscriptionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


import com.b2g.commons.FormatType;
import com.b2g.lendservice.Exceptions.LendableBookException;
import com.b2g.lendservice.Exceptions.LendableCopyException;
import com.b2g.lendservice.Exceptions.LendingException;
import com.b2g.lendservice.Exceptions.TooManyLendsException;
import com.b2g.lendservice.dto.LendableCopyEntrustRequest;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class LendingsDomainService {

    public Lending requestLending(
            UUID userId,
            LendingRequest request,
            UserSubscriptionData subscription,
            LendableBook lendableBook,
            List<Lending> activeLends
    ) {
        UUID lendableBookId = request.lendableBookId();
        UUID libraryId = request.libraryId();

        if(lendableBook == null) {
            throw new LendableBookException(
                    "Formato non è destinato al prestito o non esiste"
            );
        }

        if(lendableBook.needsToBeRetrievedAtLibrary() && libraryId == null) {
            throw new LendableBookException(
                    "Il formato scelto necessita di essere ritirato in libreria"
            );
        }

        LendingOption option =
                lendableBook.getMaxApplicableOption(subscription.getTier());

        if (option == null) {
            throw new IllegalStateException(
                    "Book not available for your subscription level"
            );
        }

        if (activeLends.size() >= subscription.getMaxConcurrentLends()) {
            throw new TooManyLendsException(
                    "Il lettore non può ricevere altri prestiti attivi"
            );
        }

        boolean alreadyLent =
                activeLends.stream()
                        .anyMatch(l ->
                                l.getCopy()
                                        .getLendableBookId()
                                        .equals(lendableBookId)
                        );

        if (alreadyLent) {
            throw new LendingException(
                    "Il lettore ha già un prestito attivo per questo formato"
            );
        }

        LendableCopy copy =
                new LendableCopy(lendableBook.getFormatId(), null);

        // Periodo dipende dal LendingOption
        LendingPeriod period = null;
        if(lendableBook.getType() != FormatType.PHYSICAL) {
            period = new LendingPeriod(
                    LocalDate.now(),
                    LocalDate.now().plusDays(option.getDurationDays())
            );
        }

        Lending lending =
                Lending.create(
                        userId,
                        option.getId(),
                        libraryId,
                        copy,
                        subscription.getTier()
                );

        if(!lendableBook.needsToBeRetrievedAtLibrary()) {
            lending.startLending(option, copy);
        }

        return lending;
    }

    public Lending entrustLendCopyToUser(
            Lending lendingToFulfill,
            LendableBook lendableBook,
            LendableCopyEntrustRequest entrustRequest
    ) {
        if(lendingToFulfill == null) {
            throw new LendingException("Lending to fulfill not found");
        }

        UUID lendableBookId =
                lendingToFulfill.getCopy().getLendableBookId();

        UUID userId = entrustRequest.getUserId();

        if(!userId.equals(lendingToFulfill.getUserId())) {
            throw new LendingException(
                    "L'utente indicato non coincide con quello del prestito"
            );
        }

        UUID libraryId = entrustRequest.getLibraryId();
        if (!libraryId.equals(lendingToFulfill.getLibraryId())) {
            throw new LendingException("Libreria indicata non coincide");
        }

        Integer copyNumber = entrustRequest.getCopyNumber();

        LendableCopy copy =
                new LendableCopy(lendableBookId, copyNumber);

        if(!copy.equals(lendingToFulfill.getCopy())) {
            throw new LendableCopyException(
                    "La copia indicata non è quella riservata per questo prestito"
            );
        }

        Optional<LendingOption> usedOption =
                lendableBook.findOption(
                        lendingToFulfill.getLendingOptionId()
                );

        LendingOption option = usedOption.orElse(null);

        if(option == null) {
            throw new LendableBookException("Lending option not found");
        }

        lendingToFulfill.startLending(option, copy);

        return lendingToFulfill;
    }

    public Lending assignCopyToLend(
            LendableCopy copy,
            UUID libraryId,
            List<Lending> activeLendings
    ) {
        if (activeLendings == null || activeLendings.isEmpty()) {
            return null;
        }

        Lending lend = decideLendingToAssignBasedOnSubscrition(activeLendings);
        lend.addCopy(copy);
        return lend;
    }

    // per ora semplice
    public Lending decideLendingToAssignBasedOnSubscrition(List<Lending> activeLendings) {
        return activeLendings.getFirst();
    }
}
