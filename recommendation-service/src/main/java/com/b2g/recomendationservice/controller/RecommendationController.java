package com.b2g.recomendationservice.controller;

import com.b2g.recomendationservice.annotations.RequireUserUUID;
import com.b2g.recomendationservice.dto.ReviewDTO;
import com.b2g.recomendationservice.model.nodes.BookNode;
import com.b2g.recomendationservice.model.nodes.Publisher;
import com.b2g.recomendationservice.model.nodes.Reader;
import com.b2g.recomendationservice.model.nodes.Writer;
import com.b2g.recomendationservice.model.relationships.PublishedBy;
import com.b2g.recomendationservice.model.relationships.WrittenBy;
import com.b2g.recomendationservice.service.JwtService;
import com.b2g.recomendationservice.service.RecommendationService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final JwtService jwtService;
    private final RecommendationService recommService;
    @GetMapping({"", "/"})
    public ResponseEntity<?> genericRecommendation(
            @RequestParam(required = false) Set<UUID> categoryIds) {
        List<BookNode> recommendationList = recommService.getGenericReccomendation(categoryIds);
        return ResponseEntity.status(HttpStatus.OK).body(recommendationList);
    }
    @GetMapping("/personalized")
    @RequireUserUUID
    public ResponseEntity<?> personalizedRecommendation(
            @RequestParam(required = false) Set<UUID> categoryIds,
            @RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7);
        if (jwt.trim().isEmpty()) {
            log.warn("Empty JWT token for role-protected endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        }

        // Validate JWT and extract claims
        Claims claims = jwtService.validateToken(jwt);
        UUID userId= jwtService.extractUserUUID(claims);
        List<BookNode> recommendationList = recommService.getPersonalizedReccomendation(userId, categoryIds);
        return ResponseEntity.status(HttpStatus.OK).body(recommendationList);
    }
    @GetMapping({"/test/creation/","/test/creation"})
    public ResponseEntity<String> testRecommendation() {
        StringBuilder log = new StringBuilder();

        try {
            log.append("=== TEST START ===\n");
            log.append("Testing recommendation system...\n");
            UUID readerId = UUID.randomUUID();
            Reader reader = new Reader();
            reader.setId(readerId);
            log.append("Creating Reader with id: ").append(readerId).append("\n");
            recommService.addReaderNode(reader);


            UUID publisherId = UUID.randomUUID();
            Publisher publisher = new Publisher();
            publisher.setId(publisherId);
            publisher.setName("Test Publisher " + publisherId.toString().substring(0, 8));
            log.append("Creating Publisher with id: ").append(publisherId).append("\n");
            recommService.addPublisherNode(publisher);


            UUID bookId = UUID.randomUUID();
            BookNode book = new BookNode();
            book.setId(bookId);
            book.setTitle("Test Book " + bookId.toString().substring(0, 8));

// Creiamo la relazione PublishedBy con la data corrente
            PublishedBy publishedBy = new PublishedBy();
            WrittenBy vrt = new WrittenBy();
            WrittenBy vrt2 = new WrittenBy();
            Writer writer1=Writer.builder().id(UUID.randomUUID()).build();
            vrt.setWriter(writer1);
            Writer writer2=Writer.builder().id(UUID.randomUUID()).build();
            vrt2.setWriter(writer2);
            List<WrittenBy> authors= List.of(vrt,vrt2);

            publishedBy.setPublisher(publisher);
            publishedBy.setPublicationDate(new Date());
            book.setPublisher(publishedBy);
            book.setAuthors(authors);


            recommService.addBookNode(book);
            log.append("Creating book : ").append(book).append("\n")
                    .append(" and publisher: ").append(publisher.getName()).append("\n");


            float rating = 4.5f;
            ReviewDTO reviewDTO = ReviewDTO.builder()
                    .readerId(readerId)
                    .bookId(bookId)
                    .rating(rating)
                    .build();
            log.append("Created ReviewDTO: ").append(reviewDTO).append("\n");


            ReviewDTO savedReview = recommService.addReview(reviewDTO);
            log.append("Saved review in graph: ").append(savedReview.toString()).append("\n");

            log.append("=== TEST END SUCCESS ===\n");
            return ResponseEntity.ok(log.toString());

        } catch (Exception e) {
            log.append("Test failed: ").append(e.getMessage()).append("\n");
            log.append("=== TEST END FAILED ===\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(log.toString());
        }
    }

}
