package com.b2g.readerservice.service;

import com.b2g.commons.UserRegistrationMessage;
import com.b2g.readerservice.dto.ReaderForm;
import com.b2g.readerservice.dto.ReaderPublicInfo;
import com.b2g.readerservice.dto.ReaderSummary;
import com.b2g.readerservice.repository.ReaderLibraryRepository;
import com.b2g.readerservice.repository.ReaderRepository;
import com.b2g.readerservice.model.Reader;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.management.InstanceAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final ReaderLibraryRepository readerLibraryRepository;

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
                .imageUrl("/"+msg.getUsername().substring(0,1).toLowerCase()+".png")
                .build();
        readerRepository.save(reader);
        log.info("New reader info added {}",msg);
    }

    public Reader addReaderOneTimeInfo(UUID userId,  ReaderForm readerForm) throws InstanceAlreadyExistsException {
        Reader reader=readerRepository.findReaderById(userId);
        if(reader.getName()==null ) {
            reader.setName(readerForm.getName());
            reader.setSurname(readerForm.getSurname());
            reader.setAddress(readerForm.getAddress());
            reader.setDescription(readerForm.getDescription());
            reader.setPhone(readerForm.getPhone());
            readerRepository.save(reader);


        }else {
            throw new InstanceAlreadyExistsException("Reader info has already been filled. Go to a library to change it.");
        }

        return reader;
    }

    public void changeReaderDescription(UUID userId, String newDescription) {
        Reader reader = readerRepository.findReaderById(userId);
        reader.setDescription(newDescription);
        readerRepository.save(reader);
    }
}
