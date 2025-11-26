package com.b2g.recomendationservice.service;

import com.b2g.commons.BookSummaryDTO;
import com.b2g.recomendationservice.dto.ReviewDTO;
import com.b2g.recomendationservice.model.nodes.*;
import com.b2g.recomendationservice.model.relationships.PublishedBy;
import com.b2g.recomendationservice.model.relationships.WrittenBy;
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
    @Value("${app.rabbitmq.service.prefix}")
    private String queueNamePrefix;
    private final TagRepository tagRepository;
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

    public Book addBookNode(BookSummaryDTO bookSumm) {

        List<Tag> tags=bookSumm.categories().stream().map(categoryDTO ->  {
                Optional<Tag> tmp = tagRepository.findById(categoryDTO.id().toString());
            return tmp.orElseGet(() -> tagRepository.save(new Tag(categoryDTO.id().toString(), categoryDTO.name())));
                }).collect(Collectors.toList()) ;
        List<String> authors=extractAuthors(bookSumm.authors());

        List<WrittenBy> writtenBy = new ArrayList<>(List.of());
        for (String author : authors) {
            Optional<Writer> aut=writerRepository.findByName(author);
            if (aut.isPresent()) {
                WrittenBy rel = new WrittenBy();
                rel.setWriter(aut.get());
                writtenBy.add(rel);
            }else
            {
                Writer writer=Writer.builder().name(author).build();
                writer= writerRepository.save(writer);
                WrittenBy rel = new WrittenBy();
                rel.setWriter(writer);
                writtenBy.add(rel);
            }
        }

        PublishedBy publishedBy = new PublishedBy();
        Optional<Publisher> publisher= publisherRepository.findByName(bookSumm.publisher());
        if (publisher.isPresent()) {
            publishedBy.setPublisher(publisher.get());
        }
        else
        {
            Publisher newPublisher = new Publisher();
            newPublisher.setName(bookSumm.publisher());
            newPublisher= publisherRepository.save(newPublisher);
            publishedBy.setPublisher(newPublisher);
        }


        Book bookNode = Book.builder()
                .id(bookSumm.id().toString())
                .tags(tags)
                .title(bookSumm.title())
                .authors(writtenBy)
                .publisher(publishedBy)
                .build();

        if (bookNode.getId() != null) {
            return bookRepository.findById(bookNode.getId())
                    .orElseGet(() -> bookRepository.save(bookNode));
        }
        return bookRepository.save(bookNode);
    }

    private List<String> extractAuthors(String authors) {
        List<String> authorsList = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(authors, ",");
        while (tokenizer.hasMoreTokens()) {
            authorsList.add(tokenizer.nextToken());
        }
        return authorsList;
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
        return reviewRepository.createReview(review.getReaderId().toString(), review.getBookId().toString(), review.getRating());
    }



    public Page<Book> getGenericRecommendation(Set<String> categoryIds, int page, int size, boolean mustHaveAll) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        System.out.println("must all: " + mustHaveAll);
        if (mustHaveAll)
            return bookRepository.findByAllTags(categoryIds,categoryIds.size(),pageable);
        else
            return bookRepository.findByTagsIdIn(categoryIds,pageable);
    }

    public Page<Book> getPersonalizedReccomendation(String userId, Set<UUID> categoryIds, Pageable pageable) {


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
