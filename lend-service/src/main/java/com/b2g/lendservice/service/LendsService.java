package com.b2g.lendservice.service;

import com.b2g.commons.FormatType;
import com.b2g.commons.LendState;
import com.b2g.lendservice.Exceptions.LendableBookException;
import com.b2g.lendservice.Exceptions.LendingException;
import com.b2g.lendservice.Exceptions.TooManyLendsException;
import com.b2g.lendservice.model.*;
import com.b2g.lendservice.repository.LendableBookRepository;
import com.b2g.lendservice.repository.LendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LendsService {

    private final LendingRepository lendingRepository;
    private final LendableBookRepository lendableBookRepository;
    private final RabbitTemplate safeRabbitTemplate;
    private final SubscriptionClient subscriptionClient;
    private final LendEventPublisher lendEventPublisher;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.routingkey.lend.request.created}")
    private String routingKeyLendRequestCreated;
    @Value("${app.rabbitmq.routingkey.lend.created}")
    private String routingKeyLendCreated;

    private static final List<LendState> ACTIVE_STATES = List.of(
            LendState.PROCESSING, LendState.RESERVATION, LendState.LENDING, LendState.LATE
    );

    /**
     * Richiesta prestito (dominio)
     */
    @Transactional
    public Lending requestLending(UUID userId, UUID lendableBookId, UUID libraryId) throws Exception {
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

        Lending lending = Lending.create(userId,option.getId(),libraryId, copy);



        if(!lendableBook.needsToBeRetrievedAtLibrary()) {
            lending.startLending(option, copy);
        }

        lendingRepository.save(lending);

        lendEventPublisher.publishLendingEventAsync(lending, lendableBook);

        return lending;
    }

    /**
     * Prestito fisico: aggiorna il copy e lo stato
     */
    @Transactional
    public Lending giveLendCopyToUser(UUID userId, UUID libraryId, UUID lendableBookId, int copyNumber) {
        LendableCopy copy = new LendableCopy(lendableBookId, copyNumber);
        Lending lendingToFulfill=lendingRepository.findByUserIdAndCopyAndStateIn(userId,copy,Set.of(LendState.AWAITING));

        if(lendingToFulfill==null) {throw new LendingException("Lending to fulfill not found");}
        LendableBook lendableBook=lendableBookRepository.findByFormatId(lendableBookId);
        Optional<LendingOption> usedOption= lendableBook.findOption(lendingToFulfill.getLendingOptionId());
        LendingOption option=usedOption.orElse(null);
        LendingPeriod period=null;
        if(option==null) {
            throw new LendableBookException("Lending option not found");
        }
        lendingToFulfill.startLending(option, copy);

//        checkLendableCopyIsStillAvailable(copy);


        Lending fulfilledLend =  lendingRepository.save(lendingToFulfill);

        return fulfilledLend;
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
    public List<LendableCopy> getReaderAwaitingLends(UUID userId) {
        return lendingRepository.findByUserIdAndState(userId, LendState.AWAITING);

    }


    public void assignCopyToLend(UUID lendId, LendableCopy copy) {
        Lending lend=lendingRepository.findById(lendId).orElse(null);
        if(lend==null) {
            throw new LendableBookException("Lend not found");
        }
        lend.addCopy(copy);
        lendingRepository.save(lend);
    }
}