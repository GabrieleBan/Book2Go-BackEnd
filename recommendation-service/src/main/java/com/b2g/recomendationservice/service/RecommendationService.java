package com.b2g.recomendationservice.service;

import com.b2g.recomendationservice.dto.BookSummaryDTO;
import com.b2g.recomendationservice.dto.ReviewDTO;
import com.b2g.recomendationservice.model.nodes.Book;
import com.b2g.recomendationservice.model.nodes.Publisher;
import com.b2g.recomendationservice.model.nodes.Reader;
import com.b2g.recomendationservice.model.nodes.Writer;
import com.b2g.recomendationservice.model.relationships.Reviews;
import com.b2g.recomendationservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final WriterRepository writerRepository;
    public  final ReaderRepository readerRepository;
    private final ReviewRepository reviewRepository;


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



    public List<BookSummaryDTO> getGenericReccomendation(Set<UUID> categoryIds) {
        return List.of();
    }

    public List<BookSummaryDTO> getPersonalizedReccomendation(UUID userId, Set<UUID> categoryIds) {
        return List.of();
    }
}
