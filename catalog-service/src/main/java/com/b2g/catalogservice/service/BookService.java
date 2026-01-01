package com.b2g.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
//
//    private final BookRepository bookRepository;
//    private final CategoryRepository categoryRepository;
//    private final BookFormatRepository bookFormatRepository;
//
//    @Value("${app.rabbitmq.exchange}")
//    private  String exchangeName;
//    @Value("${app.rabbitmq.queue.name}")
//    private String queueNamePrefix;
//    @Value("${app.rabbitmq.routingkey.book.creation}")
//    private String routingKeyParentBookCreated;
//    @Value("${rentalService.internal.url}")
//    private String baseRentalUrl;
//    private final RestTemplate restTemplate;
//
//    @Autowired
//    @Qualifier("safeRabbitTemplate")
//    private final RabbitTemplate safeRabbitTemplate;
//
//    @Transactional(readOnly = true)
//    public List<BookSummaryDTO> getAllBooks(Set<UUID> categoryIds, Pageable pageable) {
//        Page<CatalogBook> booksPage;
//
//        // Se non ci sono filtri per categoria, prendi tutti i libri con paginazione
//        if (categoryIds == null || categoryIds.isEmpty()) {
//            booksPage = bookRepository.findAll(pageable);
//        } else {
//            // Filtra i libri per le categorie specificate
//            booksPage = bookRepository.findByCategoriesIdIn(categoryIds, pageable);
//        }
//
//        return booksPage.getContent().stream()
//                .map(this::convertToBookSummaryDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public BookDetailDTO createBook(CatalogBookCreateRequestDTO request) throws NoSuchElementException{
//        // Fetch categories if provided
//        Set<Category> categories = new HashSet<>();
//        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
//            categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
//        }
//        if(categories.size()!=request.categoryIds().size()){
//            throw new NoSuchElementException("Al least one of the specified tags does not exist");
//        }
//
//        // Create Book entity from request
//        CatalogBook catalogBook = CatalogBook.builder()
//                .title(request.title())
//                .author(request.author())
//                .description(request.description())
//                .publisher(request.publisher())
//                .publicationDate(request.publicationDate())
//                .categories(categories)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .availableFormats(new ArrayList<>())
//                .build();
//
//        // Save the book first to get the ID
//        log.info("Book created: {}", catalogBook);
//        CatalogBook savedCatalogBook = bookRepository.save(catalogBook);
//        log.info("Saved book: {}", savedCatalogBook);
//
//        // Create and save book formats if provided
//        // da togliere poi
//        List<BookFormat> bookFormats = createBookFormats(request.formats(), savedCatalogBook);
//        log.info("Book formats: {}", bookFormats);
//        try {
//
//
//            try {
//                signalBookPageCreation(catalogBook);
//            } catch (Exception e) {
//                throw new RuntimeException("Errore durante il signaling: " + e.getMessage(), e);
//            }
//
//        } catch (Exception e) {
//
//            bookFormatRepository.deleteAll(bookFormats);
//            bookRepository.delete(savedCatalogBook);
//            throw e;
//        }
//        savedCatalogBook.setAvailableFormats(bookFormats);
//        savedCatalogBook = bookRepository.save(savedCatalogBook);
//        return convertToBookDetailDTO(savedCatalogBook, bookFormats);
//    }
//
//
//
//    private void signalBookPageCreation(CatalogBook catalogBook) {
//
//
//        // Invio del messaggio di registrazione con username e UUID
//        try {
//            BookSummaryDTO bookCreationMessage = BookSummaryDTO.builder()
//                    .id(catalogBook.getId())
//                    .title(catalogBook.getTitle())
//                    .author(catalogBook.getAuthor())
//                    .publisher(catalogBook.getPublisher())
//                    .coverImageUrl(catalogBook.getCoverImageUrl())
//                    .categories(catalogBook.getCategories().stream().map(category -> {
//                        return new CategoryDTO(category.getId(),category.getName());
//                    }).collect(Collectors.toSet()))
//                    .build();
//
//
//            safeRabbitTemplate.invoke(ops -> {
//                ops.execute(channel -> {
//                    // Converte il DTO in byte[]
//                    byte[] body = safeRabbitTemplate.getMessageConverter()
//                            .toMessage(bookCreationMessage, new MessageProperties())
//                            .getBody();
//
//                    // Pubblica il messaggio con mandatory=true → errori NO_ROUTE arrivano subito
//                    channel.basicPublish(
//                            exchangeName,
//                            routingKeyParentBookCreated,
//                            true,  // mandatory
//                            null,  // MessageProperties opzionale, il converter gestisce headers
//                            body
//                    );
//                    return null;
//                });
//
//                // Attende conferma sincrona dal broker
//                ops.waitForConfirmsOrDie(3000);
//
//                return null;
//            });
//
//
//        } catch (Exception e) {
//            log.error("❌ Errore nella consegna del messaggio: abort della registrazione", e);
//           throw new RuntimeException(e);
//        }
//
//
//    }
//
//
//
//    private List<BookFormat> createBookFormats(List<BookFormatCreateDTO> formatDtos, CatalogBook catalogBook) {
//        List<BookFormat> bookFormats = new ArrayList<>();
//
//        if (formatDtos != null && !formatDtos.isEmpty()) {
//            // Creazione dei BookFormat senza rental option
//            for (BookFormatCreateDTO formatDto : formatDtos) {
//                BookFormat bookFormat = BookFormat.builder()
//                        .book(catalogBook)
//                        .formatType(FormatType.valueOf(formatDto.formatType().toUpperCase()))
//                        .purchasePrice(formatDto.purchasePrice())
//                        .stockQuantity(formatDto.stockQuantity())
//                        .isAvailableForPurchase(formatDto.isAvailableForPurchase())
//                        .isAvailableForRental(formatDto.isAvailableForRental())
//                        .isAvailableOnSubscription(formatDto.isAvailableOnSubscription())
////                        .rentalOptions(new ArrayList<>()) // vuota per ora
//                        .build();
//
//                BookFormat savedBookFormat = bookFormatRepository.save(bookFormat);
//                bookFormats.add(savedBookFormat);
//            }
//
//        }
//
//        return bookFormats;
//    }
//
//    private BookDetailDTO convertToBookDetailDTO(CatalogBook catalogBook, List<BookFormat> bookFormats) {
//        // Convert to DTOs
//
//        List<CategoryDTO> categoryDTOs = catalogBook.getCategories().stream()
//                .map(category -> new CategoryDTO(category.getId(), category.getName()))
//                .collect(Collectors.toList());
//
//        List<BookFormatDTO> formatDTOs = bookFormats.stream()
//                .map(format -> new BookFormatDTO(
//                        format.getId(),
//                        format.getFormatType().name(),
//                        format.getPurchasePrice()
//
//                ))
//                .collect(Collectors.toList());
//
//        return new BookDetailDTO(
//                catalogBook.getId(),
//                catalogBook.getTitle(),
//                catalogBook.getAuthor(),
////                book.getIsbn(),
//                catalogBook.getDescription(),
//                catalogBook.getPublisher(),
//                catalogBook.getPublicationDate(),
////                book.getCoverImageUrl(),
//                categoryDTOs,
//                formatDTOs
//        );
//    }
//
//    private BookSummaryDTO convertToBookSummaryDTO(CatalogBook catalogBook) {
//        Set<CategoryDTO> categoryDTOs = catalogBook.getCategories().stream()
//                .map(category -> new CategoryDTO(category.getId(), category.getName()))
//                .collect(Collectors.toSet());
//        Map <String ,BigDecimal> prices = new HashMap<>();
//        List<BookFormat> availableFormats = catalogBook.getAvailableFormats();
//        for (BookFormat bookFormat : availableFormats) {
//            prices.put(bookFormat.getFormatType().name(), bookFormat.getPurchasePrice());
//        }
//
//        return new BookSummaryDTO(
//                catalogBook.getId(),
//                catalogBook.getTitle(),
//                catalogBook.getAuthor(),
//                catalogBook.getPublisher(),
//                catalogBook.getCoverImageUrl(),
//                prices,
//                catalogBook.getRating(),
//                categoryDTOs
//        );
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<BookDetailDTO> getBookById(UUID id) {
//        Optional<CatalogBook> bookOptional = bookRepository.findById(id);
//
//        if (bookOptional.isPresent()) {
//            CatalogBook catalogBook = bookOptional.get();
//            return Optional.of(convertToBookDetailDTO(catalogBook, catalogBook.getAvailableFormats()));
//        }
//
//        return Optional.empty();
//    }
//
//    public CatalogFormatResponse getBooksFormat(UUID bookId, UUID formatId) {
//
//        CatalogBook catalogBook = bookRepository.findById(bookId)
//                .orElseThrow(() ->
//                        new NoSuchElementException("Book with id " + bookId + " not found"));
//
//        BookFormat format = catalogBook.getAvailableFormats()
//                .stream()
//                .filter(f -> f.getId().equals(formatId))
//                .findFirst()
//                .orElseThrow(() ->
//                        new NoSuchElementException("Format " + formatId + " does not belong to book " + bookId));
//
//        return new CatalogFormatResponse(
//                catalogBook.getId(),
//                format.getId(),
//                format.getFormatType()
//        );
//    }
}



//// Metodo che invia il formato al RentalService tramite REST
//private boolean sendFormatToRentalService(BookFormat format, UUID parentBookId) {
//    try {
//        // Costruisci payload
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("formatType", format.getFormatType().name());
//        payload.put("formatId", format.getId());
//        payload.put("parentBookId", parentBookId);
//        payload.put("purchasePrice", format.getPurchasePrice());
//        payload.put("stockQuantity", format.getStockQuantity());
//        payload.put("isAvailableOnSubscription", format.isAvailableOnSubscription());
//
//        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder
//                .getRequestAttributes()).getRequest();
//
//        String bearer = req.getHeader("Authorization").substring(7);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(bearer);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
//
//        ResponseEntity<String> response = restTemplate.postForEntity(
//                baseRentalUrl+"/format/create",
//                request,
//                String.class
//        );
//
//        return response.getStatusCode().is2xxSuccessful();
//
//    } catch (Exception e) {
//        log.error("Errore invio formato {} al RentalService", format.getId(), e);
//        return false;
//    }
//}
//
//private List<String> extractAuthors(String author) {
//    String[] split = author.split(",");
//    return Arrays.asList(split);
//}