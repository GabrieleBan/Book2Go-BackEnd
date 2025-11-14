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
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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
        return readerRepository.save(reader);
    }

    public Publisher addPublisherNode(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    public Book addBookNode(Book book) {
        return bookRepository.save(book);
    }

    public Writer addWriterNode(Writer writer) {
        return writerRepository.save(writer);
    }

    public ReviewDTO addReview(ReviewDTO review)  {
        if(review.getRating()>5 || review.getRating()<0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        return reviewRepository.createReview(review.getReaderId(), review.getBookId(), review.getRating());
    }



    public List<Book> getGenericReccomendation(Set<UUID> categoryIds) {
        return List.of();
    }

    public List<Book> getPersonalizedReccomendation(UUID userId, Set<UUID> categoryIds) {
        return List.of();
    }
}
