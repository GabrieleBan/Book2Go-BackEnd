package com.b2g.lendservice.service.application;

import com.b2g.lendservice.Exceptions.BookAlreadyExistsException;
import com.b2g.lendservice.Exceptions.LendableBookException;
import com.b2g.lendservice.Exceptions.LendingOptionNotFoundException;
import com.b2g.lendservice.dto.LendingOptionDTO;
import com.b2g.lendservice.model.LendableBook;
import com.b2g.lendservice.model.LendableCopy;
import com.b2g.lendservice.model.LendingOption;
import com.b2g.lendservice.model.UserSubscriptionData;
import com.b2g.lendservice.repository.LendableBookRepository;
import com.b2g.lendservice.repository.LendingOptionRepository;
import com.b2g.lendservice.service.domain.BookDomainService;
import com.b2g.lendservice.service.infrastructure.CatalogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookApplicationService {

    private final LendableBookRepository lendableBookRepository;
    private final CatalogClient catalogClient;
    private final BookDomainService bookDomainService;

    // Crea nuovo LendableBook

    @Transactional
    public LendableBook createLendableBook(UUID bookId, UUID formatId) throws Exception {
        LendableBook existing = lendableBookRepository.findByFormatId(formatId);
        if (existing != null) {
            throw new BookAlreadyExistsException("Un formato per l prestito con lo stesso id esiste già");
        }

        CatalogClient.CatalogFormatResponse catalogData =
                catalogClient.getBookFormat(bookId, formatId);

        log.info("Lendable Book with id {} can be created", bookId);

        LendableBook newBook =
                bookDomainService.createLendableBook(bookId, formatId, catalogData.getLendingFormat());

        return lendableBookRepository.save(newBook);
    }

    // Nuova opzione lendin

    @Transactional
    public Set<LendingOption> addLendingOption(UUID formatId, LendingOptionDTO optionToAdd) {
        LendableBook book = lendableBookRepository.findByFormatId(formatId);
        if (book == null) {
            throw new LendableBookException("Lendable Book con id " + formatId + " non esiste");
        }

        bookDomainService.addLendingOption(book, optionToAdd);

        lendableBookRepository.save(book);

        log.info("Opzione  aggiunta a LendableBook {}", book.getFormatId());
        return book.getOptions();
    }

    // rimuove
    @Transactional
    public void removeLendingOption(UUID formatId, UUID optionId) {
        LendableBook book = lendableBookRepository.findByFormatId(formatId);
        if (book == null) {
            throw new LendableBookException("Lendable Book con id " + formatId + " non esiste");
        }

        bookDomainService.removeLendingOption(book, optionId);

        lendableBookRepository.save(book);

        log.info("Opzione {} rimossa da LendableBook {}", optionId, book.getFormatId());
    }

    // aggiunta nupva copia
    public LendableCopy createLendableCopy(UUID lendableBookId, Integer copyNumber) {
        return bookDomainService.createLendableCopy(lendableBookId, copyNumber);
    }

    public List<LendableBook> getLendableFormatsForBook(UUID bookId) {
        List<LendableBook> allLendableBooksForBook =
                lendableBookRepository.findAllByBookId(bookId);

        if (allLendableBooksForBook.isEmpty()) {
            throw new LendableBookException("Nessun formato prestabile per il libro indicato");
        }

        return allLendableBooksForBook;
    }

    public Set<LendingOption> getOptionsForLendableFormat(UUID formatId) {
        LendableBook book = lendableBookRepository.findByFormatId(formatId);
        if (book == null) {
            throw new LendableBookException("Lendable Book con id " + formatId + " non esiste");
        }

        log.info("book found {}", book);
        return book.getOptions();
    }
}