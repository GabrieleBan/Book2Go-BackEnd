package com.b2g.readerservice.service;

import com.b2g.commons.UserRegistrationMessage;
import com.b2g.readerservice.dto.ReaderPublicInfo;
import com.b2g.readerservice.dto.ReaderSummary;
import com.b2g.readerservice.repository.ReaderRepository;
import com.b2g.readerservice.model.Reader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;

    public List<ReaderSummary> retrieveReadersSummary(Set<UUID> readersIds) {
        List<Reader> readersList = readerRepository.findReadersById(readersIds);
        List<ReaderSummary> readerSummaries = new ArrayList<>();
        for (Reader reader : readersList) {
            System.out.println(reader);
            readerSummaries.add(ReaderSummary.fromReader(reader));
        }
        return readerSummaries;

    }

    public ReaderPublicInfo retrieveReaderFullPublicInfo(UUID userId) {
        Reader reader=readerRepository.findReadersById(Set.of(userId)).getFirst();
        return ReaderPublicInfo.fromReader(reader);
    }

    public Reader retrieveReaderById(UUID userId) {
        return readerRepository.findReaderById(userId);
    }

    public void addReaderBasicInfo(UserRegistrationMessage msg) {
        Reader reader=Reader.builder()
                .id(msg.getUuid())
                .username(msg.getUsername())
                .email(msg.getEmail())
                .build();
        readerRepository.save(reader);
        log.info("New reader info added {}",msg);
    }
}
