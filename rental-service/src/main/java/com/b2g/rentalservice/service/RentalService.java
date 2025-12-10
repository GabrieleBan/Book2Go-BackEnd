package com.b2g.rentalservice.service;

import com.b2g.commons.*;
import com.b2g.rentalservice.dto.FormatOptionLink;
import com.b2g.rentalservice.dto.RentalFormatDto;
import com.b2g.rentalservice.dto.RetrieveFormatsOptionsDTO;
import com.b2g.rentalservice.model.*;

import com.b2g.rentalservice.repository.LendingHistoryRepository;
import com.b2g.rentalservice.repository.RentalBookFormatRepository;
import com.b2g.rentalservice.repository.RentalOptionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {



    private final RabbitTemplate rabbitTemplate;
    @Value("${app.rabbitmq.exchange}")
    private  String exchangeName;
    @Value("${app.rabbitmq.queue.name}")
    private String queueNamePrefix;

    @Value("${inventoryService.internal.url}")
    private String inventoryUrl;
//    @Value("${app.rabbitmq.routingkey.book.creation}")
//    private String routingKeyParentBookCreated;

    @Autowired
    @Qualifier("safeRabbitTemplate")
    private final RabbitTemplate safeRabbitTemplate;

    @Autowired
    private RentalBookFormatRepository rentalBookFormatRepository;
//    @Autowired
//    private PhysicalBookRepository physicalBookRepository;
    @Autowired
    private RentalOptionRepository rentalOptionRepository;
//    private final PhysicalBookService physicalBookService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LendingHistoryRepository lendingHistoryRepository;
    @Value("${app.rabbitmq.routingkey.lend.requested.paymentcheck}")
    private String routingKeyPaymentlendProcessing;
    @Value("${app.rabbitmq.routingkey.lend.request.created}")
    private String routingKeyLendRequestCreated;
    @Value("${app.rabbitmq.routingkey.lend.created}")
    private String routingKeyLendCreated;
    @Value("${app.rabbitmq.routingkey.lend.failed}")
    private String routingKeyLendFailed;
    @Value("${app.rabbitmq.routingkey.reservation.created}")
    private String routingKeyLendReservationCreated;
    @Value("${app.rabbitmq.routingkey.reservation.failed}")
    private String routingKeyLendReservationFailed;
    @Value("${app.rabbitmq.routingkey.lend.ended}")
    private String routingLendExpired;


    public Set<String> addRentalFormat(@Valid RentalFormatCreationDTO formatDTO) throws Exception {
        RentalBookFormat rentalBookFormat =  RentalBookFormat.builder()
                .formatId(formatDTO.formatId())
                .bookId(formatDTO.parentBookId())
                .isAvailableOnSubscription(formatDTO.isAvailableOnSubscription())
//                .stockQuantity(formatDTO.stockQuantity())
                .formatType(FormatType.valueOf(formatDTO.formatType()))
                .build();
        log.info("RentalBookFormat added: " + rentalBookFormat);
        rentalBookFormatRepository.save(rentalBookFormat);
        log.info("RentalBookFormat saved: " + rentalBookFormat);
//        if(! (formatDTO.formatType().equalsIgnoreCase(FormatType.AUDIOBOOK.toString()) ||
//        formatDTO.formatType().equalsIgnoreCase(FormatType.EBOOK.toString()))) {
//            if(formatDTO.stockQuantity()==null || formatDTO.stockQuantity()<=0) {
//                throw new Exception("Quantity should be > 0 for physical books");
//            }
//            log.info("Aggiungendo nuovi libri fisici " + formatDTO.stockQuantity());
//            return physicalBookService.addPhysicalBooks(rentalBookFormat.getFormatId(),formatDTO.stockQuantity() );
//        }
//        else
            return new HashSet<>();
    }

    @Transactional
    public RentalOption addOrCreateOptionTo(UUID formatId, @Valid RentalOptionCreateDTO optionDTO) throws Exception {
        RentalBookFormat format = rentalBookFormatRepository.findById(formatId)
                .orElseThrow(() -> new Exception("Format not found in Rental System"));

        RentalOption option = getOrCreateNewRentalOption(optionDTO);

        format.getRentalOptions().add(option);

        // Salva dal lato "owner" della relazione
        rentalBookFormatRepository.save(format);
        return option;
    }

    private RentalOption getOrCreateNewRentalOption(@Valid RentalOptionCreateDTO optionDTO) {
        Optional<RentalOption> option=rentalOptionRepository.findByDurationDaysAndPriceAndDescription(optionDTO.durationDays(),optionDTO.price(),optionDTO.description());
        if (option.isPresent()) {
            RentalOption rentalOption = option.get();
            log.info("RentalOption found: " +  rentalOption);
            return rentalOption;
        }
        else {

            RentalOption newOption=RentalOption.builder()
                    .price(optionDTO.price())
                    .description(optionDTO.description())
                    .durationDays(optionDTO.durationDays())
                    .build();
            newOption= rentalOptionRepository.save(newOption);
            log.info("RentalOption created: " + newOption);
            return newOption;
        }
    }

    public RetrieveFormatsOptionsDTO getFormatRentOptions(UUID formatId) throws Exception {
        Optional<RentalBookFormat> tmp= rentalBookFormatRepository.findById(formatId);
        if(tmp.isPresent()) {
            RentalBookFormat rentalBookFormat = tmp.get();
            return RetrieveFormatsOptionsDTO.builder()
                    .rentalBookFormatId(formatId)
                    .parentBook(rentalBookFormat.getBookId())
                    .format(rentalBookFormat.getFormatType())
                    .options(rentalBookFormat.getRentalOptions())
                    .build();
        }
        else throw new Exception("Format not found in Rental System");


    }

    public void createNewlend(LendRequest request, UUID userId) throws Exception {
        int days=30;
        SubscriptionType type = checkUserSubscriprion(userId);
        if(request.getPaymentmethod()==null && type == SubscriptionType.UNSUBSCRIBED)
            throw new Exception("Must be subscribed to get a lend without paying");
        log.info("Lending request: " + request.getFormatId());
        RentalBookFormat bookFormat=rentalBookFormatRepository.findByFormatId(request.getFormatId());

        log.info(bookFormat.toString());
        if(!bookFormat.isAvailableOnSubscription())
            throw new Exception("Il formato richiesto non è disponibile per il prestito");

        FormatType requestedFormat= bookFormat.getFormatType();
        try {
            checkUserOtherLends(userId, type);
        }catch (Exception e){
            throw new Exception("User is not allowed to receive another lend");
        }
        if (request.getLibraryId() == null &&
                requestedFormat != FormatType.EBOOK &&
                requestedFormat != FormatType.AUDIOBOOK) {
            throw new Exception("Destination library required for physical books");
        }

        reserveRequestedFormat(bookFormat,days,userId,request.getLibraryId(),request.getPaymentmethod());
    }

    private void checkUserOtherLends(UUID userId, SubscriptionType type) throws Exception {
        if(userHasActiveLend(userId)){
            throw new Exception("User has active lends cannot request more");
        }
    }
    private static final List<LendState> ACTIVE_STATES = List.of(
            LendState.PROCESSING,
            LendState.RESERVATION,
            LendState.LENDING,
            LendState.EXPIRED
    );
    public boolean userHasActiveLend(UUID userId) {
        return lendingHistoryRepository.existsByUserIdAndStateIn(userId, ACTIVE_STATES);
    }

    private void reserveRequestedFormat(RentalBookFormat rentalBookFormat, int lendDuration,UUID userId,UUID library,PaymentMethod paymentMethod) throws Exception {
        FormatType requestedFormat=rentalBookFormat.getFormatType();
        LendState initialState;
        LocalDate startTime=null;
        LocalDate endTime=null;
        LendingHistory newLend=null;
        if(requestedFormat.equals(FormatType.AUDIOBOOK) || requestedFormat.equals(FormatType.EBOOK)) {
            if(paymentMethod==null)
            {
                initialState=LendState.LENDING;
                startTime=LocalDate.now();
                endTime=startTime.plusDays(lendDuration);

            }
            else
            {
                initialState=LendState.PROCESSING;
            }
            newLend=LendingHistory.builder()
                    .state(initialState)
                    .startDate(startTime)
                    .endDate(endTime)
                    .formatId(rentalBookFormat.getFormatId())
                    .type(rentalBookFormat.getFormatType())
                    .physBookId(null)
                    .bookId(rentalBookFormat.getBookId())
                    .userId(userId)
                    .build();

        } else if(paymentMethod==null && library!=null) {
            initialState=LendState.PROCESSING;
            newLend=LendingHistory.builder()
                    .state(initialState)
                    .formatId(rentalBookFormat.getFormatId())
                    .type(rentalBookFormat.getFormatType())
                    .bookId(rentalBookFormat.getBookId())
                    .userId(userId)
                    .build();


        }
        if(newLend==null) throw new NotImplementedException("Rental not supported yet");
        newLend=lendingHistoryRepository.save(newLend);
        try{

            notifyLendCreation(newLend,paymentMethod,library);
        }catch (Exception e){
            log.error("Error sending lend over rabbitMq rollback");
            lendingHistoryRepository.delete(newLend);
            throw new Exception("Internal architecture failure ABORTED")    ;
        }

    }




    private void notifyLendCreation(LendingHistory newLend, PaymentMethod paymentMethod, UUID libraryId) {
        LendingMessage message=LendingMessage.builder()
                .lendState(newLend.getState())
                .bookId(newLend.getBookId())
                .formatId(newLend.getFormatId())
                .formatType(newLend.getType())
                .lendId(newLend.getId())
                .userId(newLend.getUserId())
                .libraryId(libraryId)
                .paymentMethod(paymentMethod)
                .startDate(newLend.getStartDate())
                .endDate(newLend.getEndDate())
                .build();
        String routingKey = "";
        if(newLend.getState().equals(LendState.LENDING))
            routingKey=routingKeyLendCreated;
        if(newLend.getState().equals(LendState.PROCESSING))
            if(libraryId!=null)
                routingKey= routingKeyLendRequestCreated;



        if(routingKey.isEmpty())
        {
            log.error("routing key is empty, unexpected lend state is "+newLend.getState());
            throw new NotImplementedException("Routing key is empty");}
        try {
            String finalRoutingKey = routingKey;
            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {
                    // Converte il DTO in byte[]
                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(message, new MessageProperties())
                            .getBody();

                    // Pubblica il messaggio con mandatory=true → errori NO_ROUTE arrivano subito
                    channel.basicPublish(
                            exchangeName,
                            finalRoutingKey,
                            true,  // mandatory
                            null,  // MessageProperties opzionale, il converter gestisce headers
                            body
                    );
                    return null;
                });

                // Attende conferma sincrona dal broker
                ops.waitForConfirmsOrDie(3000);

                return null;
            });

    } catch (Exception e) {
        log.error("Errore nella consegna del messaggio: abort della creazione del lend", e);
        throw new RuntimeException(e);
    }



    }

//            if (newLend.getState().equals(LendState.RESERVATION))
//    routingKey=routingKeyLendReservationCreated;
//        if(newLend.getState().equals(LendState.EXPIRED))
//    routingKey=routingLendExpired;

    private FormatOptionLink findOptionValidForFormat(UUID optionId, UUID formatId) {
        RentalBookFormat format = rentalBookFormatRepository.findById(formatId)
                .orElse(null);
        if (format == null) return null;
        RentalOption option = rentalOptionRepository.findById(optionId)
                .orElse(null);
        if (option == null) return null;
        boolean present = format.getRentalOptions().stream()
                .anyMatch(o -> o.getId().equals(optionId));
        if (!present)
            return null;
        return new FormatOptionLink(format, option);
    }

    @Value("${readerService.internal.url}")
    private String readerServiceUrl;

    private SubscriptionType checkUserSubscriprion(UUID userId) throws Exception {
        String url = readerServiceUrl.replace("readerUUID", userId.toString());

        try {
            ResponseEntity<SubscriptionType> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            SubscriptionType.class
                    );

            return response.getBody() != null ? response.getBody() : SubscriptionType.UNSUBSCRIBED;

        } catch (Exception e) {
            throw new Exception("Utente non trovato o servizio Lettori non risponde");
        }
    }

    public PhysicalBookIdentifier givePhysicalBookToReader(@NotNull(message = "readerId that must receive the lend must be specified") UUID readerId, @NotNull(message = "library Id cannot be empty") UUID libraryId, @NotNull(message = "format Id cannot be empty") UUID formatId, @NotNull(message = "physical book id cannot be empty") Integer id) throws Exception {
        Optional<LendingHistory> lendToFinalize=lendingHistoryRepository.findFirstByUserIdAndFormatIdAndPhysBookIdAndStateIn(readerId,formatId,id,List.of(LendState.AWAITING));
        int lendDuration=30;
        if (lendToFinalize.isEmpty()) {
            throw new Exception("Reader has no books to retrieve");
        }
        LocalDate start=LocalDate.now();
        LendingHistory lend= lendToFinalize.get();
        PhysicalBookIdentifier physicalBookId = retrieveLendableBookFormatFromInventory(libraryId,formatId,id);

        lend.setFormatId(physicalBookId.getFormatId());
        lend.setPhysBookId(physicalBookId.getId());

        lend.setStartDate(start);
        lend.setEndDate(start.plusDays(lendDuration));

        lend.setState(LendState.LENDING);

        lend=lendingHistoryRepository.save(lend);
        notifyLendCreation(lend,null,null);

        return physicalBookId;
    }

    private PhysicalBookIdentifier retrieveLendableBookFormatFromInventory(
            @NotNull(message = "library Id cannot be empty") UUID libraryId,
            @NotNull(message = "format Id cannot be empty") UUID formatId, @NotNull(message = "physical book id cannot be empty") int id) {
        String url = inventoryUrl.concat("/lendable-books/retrieve");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("bookId", formatId)
                .queryParam("id", id)
                .queryParam("libraryId", libraryId);





        ResponseEntity<PhysicalBookIdentifier> response =
                restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PhysicalBookIdentifier>() {}
                );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        throw new IllegalStateException("No lendable copy found for this book in the selected library.");
    }

    public void markLendingAsWaiting(LendingMessage message) {

        LendingHistory lendToMark=lendingHistoryRepository.findById(message.getLendId()).orElse(null);
        lendToMark.setState(LendState.AWAITING);
        lendToMark.setPhysBookId(message.getPhysicalId());
        lendToMark.setStartDate(LocalDate.now());
        lendingHistoryRepository.save(lendToMark);
        return;

    }

    public List<PhysicalBookIdentifier> getReaderAwaitingRetrievalLends(@NotNull(message = "readerId that must receive the lend must be specified") UUID readerId) {
        List <PhysicalBookIdentifier> readerAwaitingRetrievalLends=new ArrayList<>();
        List<LendingHistory> awaitingLends= lendingHistoryRepository.findByUserIdAndState(readerId,LendState.AWAITING);
        for (LendingHistory awaitingLend : awaitingLends) {
            readerAwaitingRetrievalLends.add(new PhysicalBookIdentifier(awaitingLend.getPhysBookId(),awaitingLend.getFormatId()));
        }
        return readerAwaitingRetrievalLends;
    }

    public List<RentalFormatDto> getRentableFormats(UUID bookId) {
        List<RentalBookFormat> formats=rentalBookFormatRepository.findByBookId(bookId);
        List<RentalFormatDto> formatDtos=new ArrayList<>();
        for (RentalBookFormat format : formats) {
            RentalFormatDto dto=RentalFormatDto.builder()
                    .formatId(format.getFormatId())
                    .stockQuantity(format.getStockQuantity())
                    .isAvailableOnSubscription(format.isAvailableOnSubscription())
                    .formatType(format.getFormatType())
                    .build();
            formatDtos.add(dto);
        }
        return formatDtos;
    }
}
