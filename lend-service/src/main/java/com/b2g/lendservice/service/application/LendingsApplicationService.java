package com.b2g.lendservice.service.application;

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
import com.b2g.lendservice.service.domain.LendingsDomainService;
import com.b2g.lendservice.service.infrastructure.InventoryClient;
import com.b2g.lendservice.service.infrastructure.LendEventPublisher;
import com.b2g.lendservice.service.infrastructure.SubscriptionClient;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class LendingsApplicationService {

    private final LendingRepository lendingRepository;
    private final LendableBookRepository lendableBookRepository;
    private final SubscriptionClient subscriptionClient;
    private final LendEventPublisher lendEventPublisher;
    private final InventoryClient inventoryClient;
    private final LendingsDomainService lendingsDomainService;

    private static final List<LendState> ACTIVE_STATES = LendState.activeStates();

    @Transactional
    public Lending requestLending(UUID userId, LendingRequest request) throws Exception {
        UserSubscriptionData subscription =
                subscriptionClient.getUserSubscriptionData(userId);

        LendableBook lendableBook =
                lendableBookRepository.findByFormatId(request.lendableBookId());

        List<Lending> activeLends =
                lendingRepository.findByUserIdAndStateIn(userId, ACTIVE_STATES);

        Lending lending =
                lendingsDomainService.requestLending(
                        userId,
                        request,
                        subscription,
                        lendableBook,
                        activeLends
                );

        log.info("lending about to be saved");

        lending = lendingRepository.save(lending);

        lendEventPublisher.publishLendingEventAsync(lending, lendableBook);

        return lending;
    }

    /**
     * Prestito fisico: aggiorna il copy e lo stato
     */
    @Transactional
    public Lending entrustLendCopyToUser(UUID lendingID, LendableCopyEntrustRequest entrustRequest) {
        Lending lendingToFulfill =
                lendingRepository.findById(lendingID).orElse(null);

        LendableBook lendableBook =
                lendingToFulfill != null
                        ? lendableBookRepository.findByFormatId(
                        lendingToFulfill.getCopy().getLendableBookId()
                )
                        : null;

        Lending fulfilledLend =
                lendingsDomainService.entrustLendCopyToUser(
                        lendingToFulfill,
                        lendableBook,
                        entrustRequest
                );

        retrieveLendableCopyFromInventory(
                fulfilledLend.getCopy(),
                entrustRequest.getLibraryId()
        );

        fulfilledLend = lendingRepository.save(fulfilledLend);

        lendEventPublisher.publishLendingEventAsync(
                fulfilledLend,
                lendableBook
        );

        return fulfilledLend;
    }


    private void retrieveLendableCopyFromInventory(LendableCopy copy, UUID libraryId) {
        inventoryClient.retrieveCopy(copy, libraryId);
    }

    //Recupera prestiti in stato AWAITING

    public List<LendableCopy> getReaderAwaitingLendCopies(UUID userId, UUID libraryId) {
        List<Lending> l;
        if(libraryId==null) {
            l = lendingRepository.findByUserIdAndState(userId, LendState.AWAITING);
        } else {
            l = lendingRepository.findByUserIdAndStateAndLibraryId(
                    userId,
                    LendState.AWAITING,
                    libraryId
            );
        }
        List<LendableCopy> lendableCopyList = new ArrayList<>();
        for(Lending lending : l) {
            lendableCopyList.add(lending.getCopy());
        }
        return lendableCopyList;
    }

    @Transactional
    public void assignCopyToLend(LendableCopy copy, UUID libraryId) {
        List<Lending> activeLendings =
                lendingRepository.findByCopy_LendableBookIdAndStateAndLibraryId(
                        copy.getLendableBookId(),
                        LendState.PROCESSING,
                        libraryId
                );

        LendableBook book =
                lendableBookRepository.findByFormatId(copy.getLendableBookId());

        Lending lend =
                lendingsDomainService.assignCopyToLend(copy, libraryId, activeLendings);

        if (lend == null) {
            log.error(
                    " no processing lendings need book " +
                            copy.getLendableBookId() +
                            " at library " +
                            libraryId
            );
            return;
        }

        lendingRepository.save(lend);
        lendEventPublisher.publishLendingEventAsync(lend, book);
    }

    public List<Lending> getReaderLendings(UUID readerId, @NotNull Set<LendState> states) {
        return lendingRepository.findByUserIdAndStateIn(readerId, states);
    }

    public List<LendableCopy> getAllAwaitingLendCopies(UUID libraryId) {
        List<LendableCopy> copies = new ArrayList<>();
        List<Lending> l;
        if(libraryId!=null) {
             l = lendingRepository.findByStateAndLibraryId(LendState.AWAITING, libraryId);
        }
        else
        {
             l = lendingRepository.findByState(LendState.AWAITING);

        }
        for (Lending lending : l) {
            copies.add(lending.getCopy());
        }

        return copies;
    }
}