package com.b2g.lendservice.service.domain;

import com.b2g.commons.FormatType;
import com.b2g.lendservice.Exceptions.LendingOptionNotFoundException;
import com.b2g.lendservice.dto.LendingOptionDTO;
import com.b2g.lendservice.model.LendableBook;
import com.b2g.lendservice.model.LendableCopy;
import com.b2g.lendservice.model.LendingOption;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookDomainService {

    // Crea nuovo LendableBook

    public LendableBook createLendableBook(UUID bookId, UUID formatId, FormatType lendingFormat) {
        return new LendableBook(bookId, formatId, lendingFormat);
    }

    // Nuova opzione lendin

    public void addLendingOption(LendableBook book, LendingOptionDTO optionToAdd) {
        LendingOption option = LendingOption.create(optionToAdd);
        book.addOption(option);
    }

    // rimuove

    public void removeLendingOption(LendableBook book, UUID optionId) {
        if (!book.hasOption(optionId)) {
            throw new LendingOptionNotFoundException(optionId);
        }
        book.removeOption(optionId);
    }

    // aggiunta nupva copia

    public LendableCopy createLendableCopy(UUID lendableBookId, Integer copyNumber) {
        return new LendableCopy(lendableBookId, copyNumber);
    }
}