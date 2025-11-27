package com.b2g.rentalservice.service;

import com.b2g.rentalservice.model.PhysicalBookIdentifier;
import com.b2g.rentalservice.model.PhysicalBooks;
import com.b2g.rentalservice.repository.PhysicalBookRepository;
import com.b2g.rentalservice.repository.RentalBookFormatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalBookService {

    private final RentalBookFormatRepository rentalBookFormatRepository;
    private final PhysicalBookRepository physicalBookRepository;

//    @Transactional
    public Set<PhysicalBookIdentifier> addPhysicalBooks(UUID formatId, int quantity) {


        rentalBookFormatRepository.lockFormat(formatId);
        Integer last = physicalBookRepository.findLastId(formatId);
        int start = (last == null ? 0 : last + 1);

        Set<PhysicalBookIdentifier> result = new HashSet<>();

        for (int i = 0; i < quantity; i++) {
            int newId = start + i;

            PhysicalBookIdentifier id = new PhysicalBookIdentifier(newId, formatId);
            PhysicalBooks pb = new PhysicalBooks();
            pb.setId(id);

            physicalBookRepository.save(pb);
            result.add(id);
        }

        return result;
    }
}