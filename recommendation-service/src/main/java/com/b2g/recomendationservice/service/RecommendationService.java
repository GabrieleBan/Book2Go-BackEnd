package com.b2g.recomendationservice.service;

import com.b2g.recomendationservice.dto.BookSummaryDTO;
import com.b2g.recomendationservice.repository.BookRepository;
import com.b2g.recomendationservice.repository.PublisherRepository;
import com.b2g.recomendationservice.repository.ReaderRepository;
import com.b2g.recomendationservice.repository.WriterRepository;
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
    private final RecommendationService recommendationService;
    private final WriterRepository writerRepository;
    public  final ReaderRepository readerRepository;



    public List<BookSummaryDTO> getGenericReccomendation(Set<UUID> categoryIds) {
        return List.of();
    }

    public List<BookSummaryDTO> getPersonalizedReccomendation(UUID userId, Set<UUID> categoryIds) {
        return List.of();
    }
}
