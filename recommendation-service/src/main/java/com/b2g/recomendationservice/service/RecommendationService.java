package com.b2g.recomendationservice.service;

import com.b2g.recomendationservice.dto.ReviewDTO;
import com.b2g.recomendationservice.model.nodes.Book;
import com.b2g.recomendationservice.model.nodes.Publisher;
import com.b2g.recomendationservice.model.nodes.Reader;
import com.b2g.recomendationservice.model.nodes.Writer;
import com.b2g.recomendationservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    @Value("${app.rabbitmq.queue.name}")
    private String queueName;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final WriterRepository writerRepository;
    public  final ReaderRepository readerRepository;
    private final ReviewRepository reviewRepository;


    @RabbitListener(queues = "recommendation.review.queue")
    public void handleReviewEvents(ReviewDTO review, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        System.out.println("Routing key usata: {}"+ routingKey);
        switch (routingKey) {
            case "review.created" -> {
                System.out.println("Ricevuto messaggio di creazione review: {}"+ review);
                addReview(review);
            }
            case "review.updated" -> {
                System.out.println("Ricevuto messaggio dimodifica review: {}"+ review);
                addReview(review);
            }
        }

    }

    public Reader addReaderNode(Reader reader) {
        if (reader.getId() != null) {
            return readerRepository.findById(reader.getId())
                    .orElseGet(() -> readerRepository.save(reader));
        }
        return readerRepository.save(reader);
    }

    public Publisher addPublisherNode(Publisher publisher) {
        if (publisher.getId() != null) {
            return publisherRepository.findById(publisher.getId())
                    .orElseGet(() -> publisherRepository.save(publisher));
        }
        return publisherRepository.save(publisher);
    }

    public Book addBookNode(Book book) {
        if (book.getId() != null) {
            return bookRepository.findById(book.getId())
                    .orElseGet(() -> bookRepository.save(book));
        }
        return bookRepository.save(book);
    }

    public Writer addWriterNode(Writer writer) {
        if (writer.getId() != null) {
            return writerRepository.findById(writer.getId())
                    .orElseGet(() -> writerRepository.save(writer));
        }
        return writerRepository.save(writer);
    }

    public ReviewDTO addReview(ReviewDTO review)  {
        if(review.getRating()>5 || review.getRating()<0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        return reviewRepository.createReview(review.getReaderId(), review.getBookId(), review.getRating());
    }



    public Page<Book> getGenericRecommendation(Set<UUID> categoryIds, int page, int size, boolean mustHaveAll) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        System.out.println("must all: " + mustHaveAll);
        if (mustHaveAll)
            return bookRepository.findByAllTags(categoryIds,categoryIds.size(),pageable);
        else
            return bookRepository.findByTagsIdIn(categoryIds,pageable);
    }

    public Page<Book> getPersonalizedReccomendation(UUID userId, Set<UUID> categoryIds, Pageable pageable) {


        List<Book> byAuthorOrPublisher = bookRepository.recommendByAuthorOrPublisher(userId);
        List<Book> bySimilarReaders = bookRepository.recommendBySimilarReaders(userId);


        Set<Book> combined = new LinkedHashSet<>();
        combined.addAll(byAuthorOrPublisher);
        combined.addAll(bySimilarReaders);


        if (categoryIds != null && !categoryIds.isEmpty()) {
            combined = combined.stream()
                    .filter(b -> b.getTags().stream().anyMatch(t -> categoryIds.contains(t.getId())))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }


        List<Book> filteredList = new ArrayList<>(combined);


        int total = filteredList.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<Book> pageContent = start <= end ? filteredList.subList(start, end) : Collections.emptyList();


        return new PageImpl<>(pageContent, pageable, total);
    }
}
