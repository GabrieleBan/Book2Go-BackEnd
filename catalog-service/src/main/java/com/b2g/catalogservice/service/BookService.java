package com.b2g.catalogservice.service;

import com.b2g.catalogservice.dto.*;
import com.b2g.catalogservice.model.*;
import com.b2g.catalogservice.repository.BookRepository;
import com.b2g.catalogservice.repository.CategoryRepository;
import com.b2g.catalogservice.repository.BookFormatRepository;

import com.b2g.commons.BookSummaryDTO;
import com.b2g.commons.CategoryDTO;
import com.b2g.commons.RentalOptionCreateDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookFormatRepository bookFormatRepository;

    private final RabbitTemplate rabbitTemplate;
    @Value("${app.rabbitmq.exchange}")
    private  String exchangeName;
    @Value("${app.rabbitmq.queue.name}")
    private String queueNamePrefix;
    @Value("${app.rabbitmq.routingkey.book.creation}")
    private String routingKeyParentBookCreated;
    @Value("${rentalService.internal.url}")
    private String baseRentalUrl;
    private final RestTemplate restTemplate;

    @Autowired
    @Qualifier("safeRabbitTemplate")
    private final RabbitTemplate safeRabbitTemplate;

    @Transactional(readOnly = true)
    public List<BookSummaryDTO> getAllBooks(Set<UUID> categoryIds, Pageable pageable) {
        Page<Book> booksPage;

        // Se non ci sono filtri per categoria, prendi tutti i libri con paginazione
        if (categoryIds == null || categoryIds.isEmpty()) {
            booksPage = bookRepository.findAll(pageable);
        } else {
            // Filtra i libri per le categorie specificate
            booksPage = bookRepository.findByCategoriesIdIn(categoryIds, pageable);
        }

        return booksPage.getContent().stream()
                .map(this::convertToBookSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookDetailDTO createBook(BookCreateRequestDTO request) throws NoSuchElementException{
        // Fetch categories if provided
        Set<Category> categories = new HashSet<>();
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
        }
        if(categories.size()!=request.categoryIds().size()){
            throw new NoSuchElementException("Al least one of the specified tags does not exist");
        }

        // Create Book entity from request
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .description(request.description())
                .publisher(request.publisher())
                .publicationDate(request.publicationDate())
                .categories(categories)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .availableFormats(new ArrayList<>())
                .build();

        // Save the book first to get the ID
        log.info("Book created: {}", book);
        Book savedBook = bookRepository.save(book);
        log.info("Saved book: {}", savedBook);

        // Create and save book formats if provided
        // da togliere poi
        List<BookFormat> bookFormats = createBookFormats(request.formats(), savedBook);
        log.info("Book formats: {}", bookFormats);
        try {
            for (BookFormat format : bookFormats) {
                if (format.isAvailableForRental()) {
                    boolean ok = sendFormatToRentalService(format, savedBook.getId());
                    if (!ok) {
                        throw new RuntimeException("Fallita la creazione del formato su RentalService: " + format.getId());
                    }
                }
//                List<RentalOption> rentalOptionsEntities = new ArrayList<>();
//                for (RentalOption option : format.getRentalOptions()) {
//                    RentalOption rentalOption= sendRentalOptionToRentalService(format.getId(), option);
//                    if (rentalOption == null) {
//                        throw new RuntimeException("Fallita la creazione del RentalOption per format: " + format.getId());
//                    }
//
//
//                    RentalOption rentalOptionEntity = RentalOption.builder()
//                            .id(rentalOption.getId())
//                            .bookFormat(format)
//                            .durationDays(rentalOption.getDurationDays())
//                            .price(rentalOption.getPrice())
//                            .description(rentalOption.getDescription())
//                            .build();
//
//                    rentalOptionsEntities.add(rentalOptionEntity);
//                }
//
//
//                format.setRentalOptions(rentalOptionsEntities);

            }

            try {
                signalBookCreation(book);
            } catch (Exception e) {
                throw new RuntimeException("Errore durante il signaling: " + e.getMessage(), e);
            }

        } catch (Exception e) {

            bookFormatRepository.deleteAll(bookFormats);
            bookRepository.delete(savedBook);
            throw e;
        }
        savedBook.setAvailableFormats(bookFormats);
        savedBook = bookRepository.save(savedBook);
        return convertToBookDetailDTO(savedBook, bookFormats);
    }

//    private RentalOption sendRentalOptionToRentalService(UUID bookFormatId, RentalOption option) {
//        String url =  baseRentalUrl+"/format/add-option/"+ bookFormatId; // completa l'URL con l'ID del format
//
//        // Corpo della richiesta
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("durationDays", option.getDurationDays());
//        requestBody.put("price", option.getPrice());
//        requestBody.put("description", option.getDescription());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//        try {
//            ResponseEntity<RentalOptionDTO> response = restTemplate.postForEntity(url, requestEntity, RentalOptionDTO.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                RentalOptionDTO created = response.getBody();
//                option.setId(created.id());
//                return option;
//            } else {
//                throw new RuntimeException("RentalService returned non-ok status: " + response.getStatusCode());
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Errore durante l'invio della RentalOption al RentalService: " + e.getMessage(), e);
//        }
//    }

    private void signalBookCreation(Book book) {


        // Invio del messaggio di registrazione con username e UUID
        try {
            BookSummaryDTO bookCreationMessage = BookSummaryDTO.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .publisher(book.getPublisher())
                    .coverImageUrl(book.getCoverImageUrl())
                    .categories(book.getCategories().stream().map(category -> {
                        return new CategoryDTO(category.getId(),category.getName());
                    }).collect(Collectors.toSet()))
                    .build();


            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {
                    // Converte il DTO in byte[]
                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(bookCreationMessage, new MessageProperties())
                            .getBody();

                    // Pubblica il messaggio con mandatory=true → errori NO_ROUTE arrivano subito
                    channel.basicPublish(
                            exchangeName,
                            routingKeyParentBookCreated,
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
            log.error("❌ Errore nella consegna del messaggio: abort della registrazione", e);
           throw new RuntimeException(e);
        }


    }


    // Metodo che invia il formato al RentalService tramite REST
    private boolean sendFormatToRentalService(BookFormat format, UUID parentBookId) {
        try {
            // Costruisci payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("formatType", format.getFormatType().name());
            payload.put("formatId", format.getId());
            payload.put("parentBookId", parentBookId);
            payload.put("purchasePrice", format.getPurchasePrice());
            payload.put("stockQuantity", format.getStockQuantity());
            payload.put("isAvailableOnSubscription", format.isAvailableOnSubscription());

//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(); // o gestisci tramite service auth
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();

            String bearer = req.getHeader("Authorization").substring(7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearer);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseRentalUrl+"/format/create",
                    request,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Errore invio formato {} al RentalService", format.getId(), e);
            return false;
        }
    }

    private List<String> extractAuthors(String author) {
        String[] split = author.split(",");
        return Arrays.asList(split);
    }


    private List<BookFormat> createBookFormats(List<BookFormatCreateDTO> formatDtos, Book book) {
        List<BookFormat> bookFormats = new ArrayList<>();

        if (formatDtos != null && !formatDtos.isEmpty()) {
            // Creazione dei BookFormat senza rental option
            for (BookFormatCreateDTO formatDto : formatDtos) {
                BookFormat bookFormat = BookFormat.builder()
                        .book(book)
                        .formatType(FormatType.valueOf(formatDto.formatType().toUpperCase()))
                        .purchasePrice(formatDto.purchasePrice())
                        .stockQuantity(formatDto.stockQuantity())
                        .isAvailableForPurchase(formatDto.isAvailableForPurchase())
                        .isAvailableForRental(formatDto.isAvailableForRental())
                        .isAvailableOnSubscription(formatDto.isAvailableOnSubscription())
//                        .rentalOptions(new ArrayList<>()) // vuota per ora
                        .build();

                BookFormat savedBookFormat = bookFormatRepository.save(bookFormat);
                bookFormats.add(savedBookFormat);
            }

            // Aggiungo le rental option solo per
//            for (int i = 0; i < bookFormats.size(); i++) {
//                BookFormat savedFormat = bookFormats.get(i);
//                BookFormatCreateDTO formatDto = formatDtos.get(i);
//
//                if (formatDto.rentalOptions() != null && !formatDto.rentalOptions().isEmpty()) {
//                    List<RentalOption> rentalOptions = new ArrayList<>();
//                    for (RentalOptionCreateDTO rentalOptionDto : formatDto.rentalOptions()) {
//                        RentalOption rentalOption = RentalOption.builder()
//                                .bookFormat(savedFormat)
//                                .durationDays(rentalOptionDto.durationDays())
//                                .price(rentalOptionDto.price())
//                                .description(rentalOptionDto.description())
//                                .build();
//
//                        rentalOptions.add(rentalOption);
//                    }
//
//                    // Aggiorna il BookFormat con le rental option, **senza salvarle**
//                    savedFormat.setRentalOptions(rentalOptions);
//                }
//            }
        }

        return bookFormats;
    }

    private BookDetailDTO convertToBookDetailDTO(Book book, List<BookFormat> bookFormats) {
        // Convert to DTOs

        List<CategoryDTO> categoryDTOs = book.getCategories().stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .collect(Collectors.toList());

        List<BookFormatDTO> formatDTOs = bookFormats.stream()
                .map(format -> new BookFormatDTO(
                        format.getId(),
                        format.getFormatType().name(),
                        format.getPurchasePrice(),
                        format.isAvailableForPurchase(),
                        format.isAvailableForRental(),
                        format.isAvailableOnSubscription()
//                        format.getRentalOptions().stream()
//                                .map(rentalOption -> new RentalOptionDTO(
//                                        rentalOption.getId(),
//                                        rentalOption.getDurationDays(),
//                                        rentalOption.getPrice(),
//                                        rentalOption.getDescription()
//                                ))
//                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new BookDetailDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublisher(),
                book.getPublicationDate(),
                book.getCoverImageUrl(),
                categoryDTOs,
                formatDTOs
        );
    }

    private BookSummaryDTO convertToBookSummaryDTO(Book book) {
        Set<CategoryDTO> categoryDTOs = book.getCategories().stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .collect(Collectors.toSet());
        Map <String ,BigDecimal> prices = new HashMap<>();
        List<BookFormat> availableFormats = book.getAvailableFormats();
        for (BookFormat bookFormat : availableFormats) {
            prices.put(bookFormat.getFormatType().name(), bookFormat.getPurchasePrice());
        }

        return new BookSummaryDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getCoverImageUrl(),
                prices,
                book.getRating(),
                categoryDTOs
        );
    }

    @Transactional(readOnly = true)
    public Optional<BookDetailDTO> getBookById(UUID id) {
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            return Optional.of(convertToBookDetailDTO(book, book.getAvailableFormats()));
        }

        return Optional.empty();
    }
}
