package com.b2g.recomendationservice.controller;

import com.b2g.recomendationservice.dto.ReviewDTO;
import com.b2g.recomendationservice.model.nodes.*;
import com.b2g.recomendationservice.model.relationships.PublishedBy;
import com.b2g.recomendationservice.model.relationships.WrittenBy;
import com.b2g.recomendationservice.service.RecommendationService;
import com.b2g.recomendationservice.service.RemoteJwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Service
class ReviewPublisher {
    @Value("${app.rabbitmq.exchange}")
    private  String topicExchange;
    private final RabbitTemplate rabbitTemplate;

    public ReviewPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendReview(ReviewDTO review) {
        rabbitTemplate.convertAndSend(topicExchange, "review.created", review);
    }
}

@Slf4j
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RemoteJwtService remoteJwtService;
    private final RecommendationService recommService;

    private final ReviewPublisher testService;

    @GetMapping({"", "/"})
    public ResponseEntity<?> genericRecommendation(
            @RequestParam(required = true) Set<String> categoryIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean mustHaveAll) {

        // Chiama il service passando page e size
        Page<Book> recommendationPage = recommService.getGenericRecommendation(categoryIds, page, size, mustHaveAll);

        // Restituisce la pagina completa, comprensiva di info su pagine e totale elementi
        return ResponseEntity.ok(recommendationPage);
    }

    @GetMapping("/personalized")
//    @RequireUserUUID
    public ResponseEntity<?> personalizedRecommendation(
            @RequestParam(required = false) Set<UUID> categoryIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = remoteJwtService.extractUserUUID(claims);

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> recommendationPage = recommService.getPersonalizedReccomendation(userId.toString(), categoryIds, pageable);

        return ResponseEntity.ok(recommendationPage);
    }

    @GetMapping({"/test/creation/","/test/creation"})
    public ResponseEntity<String> graphTest() {
        StringBuilder log = new StringBuilder();

        try {
            log.append("=== TEST START ===\n");
            log.append("Testing recommendation system...\n");

            // ==========================
            // FIXED READERS
            // ==========================
            UUID[] readerIds = {
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    UUID.fromString("9bfb7c46-7ddd-4aed-a89f-07a9bbbc13cc")
            };
            String[] readerNames = {"Mario Rossi", "Giulia Bianchi", "Luca Ferrari"};

            List<Reader> readers = new ArrayList<>();
            for (int i = 0; i < readerIds.length; i++) {
                Reader r = new Reader();
                r.setId(readerIds[i].toString());
                recommService.addReaderNode(r);
                readers.add(r);
                log.append("Created Reader: ").append(readerNames[i])
                        .append(" (").append(readerIds[i]).append(")\n");
            }

            // ==========================
            // FIXED PUBLISHER
            // ==========================
            UUID publisherId = UUID.fromString("00000000-0000-0000-0000-000000000010");
            Publisher publisher = new Publisher();
            publisher.setId(publisherId.toString());
            publisher.setName("Mondadori");
            recommService.addPublisherNode(publisher);
            log.append("Created Publisher: ").append(publisher.getName())
                    .append(" (").append(publisherId).append(")\n");

            // ==========================
            // FIXED BOOKS
            // ==========================
            String[] bookTitles = {
                    "Il Segreto del Bosco", "Viaggio nella Galassia", "La Casa delle Ombre",
                    "Il Profumo del Mare", "Cronache di un Mago", "L’Ultimo Re",
                    "La Verità Nascosta", "Città di Vetro", "Fuga dal Futuro", "Il Sentiero dei Ricordi"
            };

            String[][] authorsNames = {
                    {"Paolo Bianchi"}, {"Luca Ferri", "Giulia Moretti"}, {"Marco Neri"},
                    {"Chiara Conti"}, {"Elena Galli"}, {"Andrea Fabbri"},
                    {"Sara Fontana", "Paolo Rizzi"}, {"Matteo Lodi"}, {"Francesca Villa"}, {"Giorgio Serra"}
            };

            String[][] tagsNames = {
                    {"Fantasy", "Avventura"}, {"Sci-Fi", "Spazio"}, {"Horror"},
                    {"Romanzo", "Mare"}, {"Fantasy"}, {"Storico"},
                    {"Thriller"}, {"Urban Fantasy"}, {"Dystopia"}, {"Drammatico", "Romantico"}
            };

            UUID[] bookIds = {
                    UUID.fromString("00000000-0000-0000-0000-000000000101"),
                    UUID.fromString("00000000-0000-0000-0000-000000000102"),
                    UUID.fromString("00000000-0000-0000-0000-000000000103"),
                    UUID.fromString("00000000-0000-0000-0000-000000000104"),
                    UUID.fromString("00000000-0000-0000-0000-000000000105"),
                    UUID.fromString("00000000-0000-0000-0000-000000000106"),
                    UUID.fromString("00000000-0000-0000-0000-000000000107"),
                    UUID.fromString("00000000-0000-0000-0000-000000000108"),
                    UUID.fromString("00000000-0000-0000-0000-000000000109"),
                    UUID.fromString("00000000-0000-0000-0000-00000000010a")
            };

            // ==========================
            // CREATE BOOKS
            // ==========================
            for (int i = 0; i < 10; i++) {
                Book book = new Book();
                book.setId(bookIds[i].toString());
                book.setTitle(bookTitles[i]);

                // AUTHORS
                List<WrittenBy> authors = new ArrayList<>();
                for (int j = 0; j < authorsNames[i].length; j++) {
                    Writer w = Writer.builder()
                            .id(UUID.fromString(String.format("00000000-0000-0000-0000-0000000002%02d", i*2+j+1)).toString())
                            .build();
                    WrittenBy rel = new WrittenBy();
                    rel.setWriter(w);
                    authors.add(rel);
                }
                book.setAuthors(authors);

                // PUBLISHER RELATION
                PublishedBy pubRel = new PublishedBy();
                pubRel.setPublisher(publisher);
                pubRel.setPublicationDate(new Date());
                book.setPublisher(pubRel);

                // TAGS
                List<Tag> tags = new ArrayList<>();
                for (int t = 0; t < tagsNames[i].length; t++) {
                    tags.add(new Tag(
                            UUID.fromString(String.format("00000000-0000-0000-0000-0000000004%02d", i*2+t+1)).toString(),
                            tagsNames[i][t]
                    ));
                }
                book.setTags(tags);

                recommService.addBookNode(book);

                // LOG dettagliato solo per il primo libro
                if (i == 0) {
                    log.append("\nCreated Book: ").append(book.getTitle())
                            .append(" (").append(book.getId()).append(")\n");
                    log.append("  Authors: ");
                    for (WrittenBy ab : authors) log.append(ab.getWriter().getId()).append(" ");
                    log.append("\n  Publisher: ").append(publisher.getName())
                            .append(" (").append(publisher.getId()).append(")\n");
                    log.append("  Tags: ");
                    for (Tag tag : tags) log.append(tag.getName()).append("[").append(tag.getId()).append("] ");
                    log.append("\n");
                }
            }

            // ==========================
            // CREATE REVIEWS (uno per lettore)
            // ==========================
            for (int i = 0; i < readers.size(); i++) {
                ReviewDTO reviewDTO = ReviewDTO.builder()
                        .readerId(readerIds[i])
                        .bookId(bookIds[i])
                        .rating(4.0f + i * 0.5f) // rating leggermente diverso
                        .build();
                recommService.addReview(reviewDTO);
                log.append("Saved Review for Reader ").append(readerNames[i])
                        .append(" on Book ").append(bookTitles[i]).append("\n");
            }

            log.append("=== TEST END SUCCESS ===\n");
            return ResponseEntity.ok(log.toString());

        } catch (Exception e) {
            log.append("Test failed: ").append(e.getMessage()).append("\n");
            log.append("=== TEST END FAILED ===\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(log.toString());
        }
    }

    @GetMapping("/test/creation/{routingKey}")
    public ResponseEntity<String> queueTests(@PathVariable String routingKey) {
        try {
            testService.sendReview(ReviewDTO.builder()
                    .readerId(UUID.randomUUID())
                    .bookId(UUID.randomUUID())
                    .rating(4.5f)
                    .build());
            return ResponseEntity.status(HttpStatus.OK).body("RabbitMQ sent the message, control the logs and the gui");

        }catch (MessagingException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("RabbitMQ failed to send message");
        }


    }

}
