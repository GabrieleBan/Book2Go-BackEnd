package com.b2g.readerservice.service;

import com.b2g.commons.*;
import com.b2g.readerservice.dto.ReaderForm;
import com.b2g.readerservice.dto.ReaderPublicInfo;
import com.b2g.readerservice.dto.ReaderSpecifications;
import com.b2g.readerservice.dto.ReaderSummary;
import com.b2g.readerservice.model.*;
import com.b2g.readerservice.repository.ReaderLibraryRepository;
import com.b2g.readerservice.repository.ReaderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.management.InstanceAlreadyExistsException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final ReaderLibraryRepository readerLibraryRepository;
    private final RabbitTemplate safeRabbitTemplate;
    @Value("${app.rabbitmq.routing-key.review.authorized}")
    private  String bookPossessionCheckedRoutingKey;

    @Value("${app.rabbitmq.exchange}")
    private  String exchangeName;


    public List<ReaderSummary> retrieveReadersSummary(Set<UUID> readersIds) {
        List<Reader> readersList = readerRepository.findReadersById(readersIds);
        List<ReaderSummary> readerSummaries = new ArrayList<>();
        for (Reader reader : readersList) {
            System.out.println(reader);
            readerSummaries.add(ReaderSummary.fromReader(reader));
        }
        return readerSummaries;

    }

    public ReaderPublicInfo retrieveReaderFullPublicInfo(UUID userId) {
        Reader reader=readerRepository.findReadersById(Set.of(userId)).getFirst();
        return ReaderPublicInfo.fromReader(reader);
    }

    public Reader retrieveReaderById(UUID userId) {
        return readerRepository.findReaderById(userId);
    }

    public void addReaderBasicInfo(UserRegistrationMessage msg) {
        Reader reader=Reader.builder()
                .id(msg.getUuid())
                .username(msg.getUsername())
                .email(msg.getEmail())
                .imageUrl("/"+msg.getUsername().substring(0,1).toLowerCase()+".png")
                .build();
        readerRepository.save(reader);
        log.info("New reader info added {}",msg);
    }

    public Reader addReaderOneTimeInfo(UUID userId,  ReaderForm readerForm) throws InstanceAlreadyExistsException {
        Reader reader=readerRepository.findReaderById(userId);
        if(reader.getName()==null ) {
            reader.setName(readerForm.getName());
            reader.setSurname(readerForm.getSurname());
            reader.setAddress(readerForm.getAddress());
            reader.setDescription(readerForm.getDescription());
            reader.setPhone(readerForm.getPhone());
            readerRepository.save(reader);


        }else {
            throw new InstanceAlreadyExistsException("Reader info has already been filled. Go to a library to change it.");
        }

        return reader;
    }

    public void changeReaderDescription(UUID userId, String newDescription) {
        Reader reader = readerRepository.findReaderById(userId);
        reader.setDescription(newDescription);
        readerRepository.save(reader);
    }

    public SubscriptionType getReaderSubscription(UUID readerId) throws Exception {

        Reader r= readerRepository.findReaderById(readerId);
        if(r==null) {
            throw new Exception("Reader "+ readerId +" not found ");
        }
        return r.getSubscription();
    }

    public void addLendToLibrary(LendingMessage message) {
        UUID userId = message.getUserId();
        UUID archetypeBookId = message.getBookId();
        UUID formatId = message.getFormatId();

        PersonalLibraryBookItemId itemId = new PersonalLibraryBookItemId();
        itemId.setUserId(userId);
        itemId.setArchetypeBookId(archetypeBookId);
        itemId.setFormatId(formatId);

        Optional<PersonalLibraryBookItem> optionalItem =
                readerLibraryRepository.findById(itemId);

        PersonalLibraryBookItem item;

        if (optionalItem.isPresent()) {

            item = optionalItem.get();
            item.setStartDate(LocalDate.now());
            item.setExpirationDate(message.getEndDate());
            item.setState(BookOwnershipState.Ongoing);
        } else {

            item = new PersonalLibraryBookItem();
            item.setId(itemId);
            item.setFormatType(message.getFormatType());
            item.setStartDate(LocalDate.now());
            item.setExpirationDate(message.getEndDate());
            item.setState(BookOwnershipState.Ongoing);
        }

        readerLibraryRepository.save(item);
    }
    public void markLendAsExpired(LendingMessage message) {
        UUID userId = message.getUserId();
        UUID archetypeBookId = message.getBookId();
        UUID formatId = message.getFormatId();

        PersonalLibraryBookItemId itemId = new PersonalLibraryBookItemId();
        itemId.setUserId(userId);
        itemId.setArchetypeBookId(archetypeBookId);
        itemId.setFormatId(formatId);

        Optional<PersonalLibraryBookItem> optionalItem = readerLibraryRepository.findById(itemId);

        if (optionalItem.isPresent()) {
            PersonalLibraryBookItem item = optionalItem.get();
            item.setState(BookOwnershipState.Late); // segna il prestito come scaduto
            readerLibraryRepository.save(item);
            log.info("Lend scaduto aggiornato per utente {} e libro {}", userId, archetypeBookId);
        } else {
            log.warn("Nessun PersonalLibraryBookItem trovato per utente {} e libro {} da marcare come scaduto", userId, archetypeBookId);
        }
    }



    public void handleReviewAuthorizationRequest(ReviewConfirmationDTO message) {

        ReaderBookPossessionResultDTO result =
                new ReaderBookPossessionResultDTO();

        result.setReviewId(message.getReviewId());
        result.setUserId(message.getUserId());
        result.setBookId(message.getBookId());

        Optional<PersonalLibraryBookItem> lastItemOpt =
                readerLibraryRepository
                        .findTopByIdUserIdAndIdArchetypeBookIdOrderByExpirationDateDesc(
                                message.getUserId(),
                                message.getBookId()
                        );

        lastItemOpt
                .map(PersonalLibraryBookItem::getStartDate)
                .ifPresent(result::setStartDate);

        sendPossessionResult(result);
    }
    private void sendPossessionResult(ReaderBookPossessionResultDTO result) {
        safeRabbitTemplate.convertAndSend(
                exchangeName,
                bookPossessionCheckedRoutingKey,
                result
        );
    }

    public List<ReaderPublicInfo> retrieveReader(
            String username,
            Address address,
            String name,
            String surname,
            String phone,
            String email
    ) {

        Specification<Reader> spec = Specification.<Reader>unrestricted()
                .and(ReaderSpecifications.usernameEquals(username))
                .and(ReaderSpecifications.nameLike(name))
                .and(ReaderSpecifications.surnameLike(surname))
                .and(ReaderSpecifications.emailEquals(email))
                .and(ReaderSpecifications.phoneEquals(phone));
        List<Reader> readers = readerRepository.findAll(spec);
        if (readers.isEmpty()) {
            throw new EntityNotFoundException("Reader not found");
        }
        List<ReaderPublicInfo> toReturn = new ArrayList<>();
        for(Reader reader : readers) {
            toReturn.add(ReaderPublicInfo.fromReader(reader));
        }
        return toReturn;
    }

}
