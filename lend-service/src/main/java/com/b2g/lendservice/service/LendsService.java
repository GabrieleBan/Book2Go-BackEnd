package com.b2g.lendservice.service;

import com.b2g.commons.FormatType;
import com.b2g.commons.LendState;
import com.b2g.lendservice.Exceptions.LendableBookException;
import com.b2g.lendservice.Exceptions.LendableCopyException;
import com.b2g.lendservice.Exceptions.LendingException;
import com.b2g.lendservice.Exceptions.TooManyLendsException;
import com.b2g.lendservice.dto.LendableCopyEntrustRequest;
import com.b2g.lendservice.dto.LendingRequest;
import com.b2g.lendservice.model.*;
import com.b2g.lendservice.repository.LendableBookRepository;
import com.b2g.lendservice.repository.LendingRepository;
import com.b2g.lendservice.service.infrastructure.InventoryClient;
import com.b2g.lendservice.service.infrastructure.LendEventPublisher;
import com.b2g.lendservice.service.infrastructure.SubscriptionClient;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LendsService {

    private final LendingRepository lendingRepository;
    private final LendableBookRepository lendableBookRepository;
    private final SubscriptionClient subscriptionClient;
    private final LendEventPublisher lendEventPublisher;
    private final InventoryClient inventoryClient;

    private static final List<LendState> ACTIVE_STATES = LendState.activeStates();

    @Transactional
    public Lending requestLending(UUID userId, LendingRequest request) throws Exception {
        UUID lendableBookId=request.lendableBookId();
        UUID libraryId=request.libraryId();
        UserSubscriptionData subscription = subscriptionClient.getUserSubscriptionData(userId);
        LendableBook lendableBook=lendableBookRepository.findByFormatId(lendableBookId);
        if(lendableBook==null) {throw new LendableBookException("Formato non è destinato al prestito o non esiste");
        }
        if(lendableBook.needsToBeRetrievedAtLibrary() && libraryId==null) {
            throw new LendableBookException("Il formato scelto necessita di essere ritirato in libreria");
        }
        LendingOption option = lendableBook.getMaxApplicableOption(subscription.getTier());
        if (option == null) {
            throw new IllegalStateException("Book not available for your subscription level");
        }

        validateConcurrentLends(userId, lendableBook.getFormatId(), subscription);

        LendableCopy copy = new LendableCopy(lendableBook.getFormatId(), null);

        // Periodo dipende dal LendingOption
        LendingPeriod period = null;
        if(lendableBook.getType()!= FormatType.PHYSICAL) {
            period= new LendingPeriod(LocalDate.now(), LocalDate.now().plusDays(option.getDurationDays()));
        }

        Lending lending = Lending.create(userId,option.getId(),libraryId, copy,subscription.getTier());



        if(!lendableBook.needsToBeRetrievedAtLibrary()) {
            lending.startLending(option, copy);
        }
        log.info("lending about to be saved");
        lending=lendingRepository.save(lending);

        lendEventPublisher.publishLendingEventAsync(lending, lendableBook);

        return lending;
    }

    /**
     * Prestito fisico: aggiorna il copy e lo stato
     */
    @Transactional
    public Lending entrustLendCopyToUser(UUID lendingID, LendableCopyEntrustRequest entrustRequest) {
        Lending lendingToFulfill = lendingRepository.findById(lendingID).orElse(null);
        if(lendingToFulfill==null) {throw new LendingException("Lending to fulfill not found");}
        UUID lendableBookId= lendingToFulfill.getCopy().getLendableBookId();
        UUID userId= entrustRequest.getUserId();

        if(!userId.equals(lendingToFulfill.getUserId())) {
            throw new LendingException("L'utente indicato non coincide con quello del prestito");
        }
        UUID libraryId= entrustRequest.getLibraryId();
//        if(libraryId!=lendingToFulfill.getLibraryId()) {
//            throw new LendingException("Libreria indicata non coincide");
//        }

        Integer copyNumber= entrustRequest.getCopyNumber();

        LendableCopy copy = new LendableCopy(lendableBookId, copyNumber);
        log.info(copy.toString());
        log.info(lendingToFulfill.getCopy().toString());
        if(!copy.equals(lendingToFulfill.getCopy())) {
            throw new LendableCopyException("La copia indicata non è quella riservata per questo prestito");
        }
        LendableBook lendableBook=lendableBookRepository.findByFormatId(lendableBookId);
        Optional<LendingOption> usedOption= lendableBook.findOption(lendingToFulfill.getLendingOptionId());
        LendingOption option=usedOption.orElse(null);
        LendingPeriod period=null;
        if(option==null) {
            throw new LendableBookException("Lending option not found");
        }
        lendingToFulfill.startLending(option, copy);

        retrieveLendableCopyFromInventory(copy,libraryId);


        Lending fulfilledLend =  lendingRepository.save(lendingToFulfill);

        return fulfilledLend;
    }

    private void retrieveLendableCopyFromInventory(LendableCopy copy, UUID libraryId) {
        inventoryClient.retrieveCopy(copy,libraryId);
    }

    /**
     * Check libri già prestati non superino massimo + non posso prestare lo stesso tipo di libro alla stessa persona in contemporanea
     */
    public void validateConcurrentLends(UUID userId, UUID lendableBookId, UserSubscriptionData subscription) {
        List<Lending> activeLends = lendingRepository.findByUserIdAndStateIn(userId, ACTIVE_STATES);

        if (activeLends.size() >= subscription.getMaxConcurrentLends()) {
            throw new TooManyLendsException("Il lettore non può ricevere altri prestiti attivi");
        }

        boolean alreadyLent = activeLends.stream()
                .anyMatch(l -> l.getCopy().getLendableBookId().equals(lendableBookId));
        if (alreadyLent) {
            throw new LendingException("Il lettore ha già un prestito attivo per questo formato");
        }
    }

    /**
     * Recupera prestiti in stato AWAITING
     */
    public List<LendableCopy> getReaderAwaitingLendCopies(UUID userId,UUID libraryId) {
        List<Lending> l;
        if(libraryId==null) {
            l=lendingRepository.findByUserIdAndState(userId, LendState.AWAITING);
        }else {
            l=lendingRepository.findByUserIdAndStateAndLibraryId(userId, LendState.AWAITING,libraryId);
        }
        List<LendableCopy> lendableCopyList=new ArrayList<>();
        for(Lending lending:l) {
            lendableCopyList.add(lending.getCopy());
        }
        return lendableCopyList;

    }

    @Transactional
    public void assignCopyToLend(LendableCopy copy,UUID libraryId) {
        List<Lending> activeLendings=lendingRepository.findByCopy_LendableBookIdAndStateAndLibraryId(copy.getLendableBookId(),LendState.PROCESSING,libraryId);
        LendableBook book= lendableBookRepository.findByFormatId(copy.getLendableBookId());
        if(activeLendings==null || activeLendings.isEmpty()) {
            log.error(" no processing lendings need book " + copy.getLendableBookId()+" at library " + libraryId);
            return;
        }
        Lending lend= decideLendingToAssignBasedOnSubscrition(activeLendings);
        lend.addCopy(copy);
        lendingRepository.save(lend);

        lendEventPublisher.publishLendingEventAsync(lend,book);

    }

    private Lending decideLendingToAssignBasedOnSubscrition(List<Lending> activeLendings) {
        return activeLendings.getFirst();
    }

    public List<Lending> getReaderLendings(UUID readerId, @NotNull Set<LendState> states) {
        return lendingRepository.findByUserIdAndStateIn(readerId,  states);

    }
}