package com.b2g.lendservice.service;

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
public class BookService {

    private final LendableBookRepository lendableBookRepository;
    private final LendingOptionRepository lendingOptionRepository;
    private final CatalogClient catalogClient;

    /**
     * Crea nuovo LendableBook
     */
    @Transactional
    public LendableBook createLendableBook(UUID bookId, UUID formatId) throws Exception {
        LendableBook book= lendableBookRepository.findByFormatId(formatId);
        if(book!=null) {throw new BookAlreadyExistsException("Un formato per l prestito con lo stesso id esiste già");}
        CatalogClient.CatalogFormatResponse catalogData = catalogClient.getBookFormat(bookId, formatId);
        log.info("Lendable Book with id {} can be created", bookId);
        LendableBook newBook=new LendableBook(bookId,formatId,catalogData.getLendingFormat());
        newBook=lendableBookRepository.save(newBook);
        return newBook;
    }

    /**
     * Aggiunge opzione
     */
    @Transactional
    public Set<LendingOption> addLendingOption(UUID formatId, LendingOptionDTO optionToAdd) throws LendableBookException {
        LendableBook book = lendableBookRepository.findByFormatId(formatId);
        if (book == null) {throw new LendableBookException("Lendable Book con id "+formatId+" non esiste");}
        LendingOption option = LendingOption.create(optionToAdd);
        book.addOption(option);
        book=lendableBookRepository.save(book);
        log.info("Opzione {} aggiunta a LendableBook {}", option.getId(), book.getFormatId());
        return book.getOptions();
    }

    /**
     * Rimuove opzione
     */
    @Transactional
    public void removeLendingOption(UUID formatId, UUID optionId) {
        LendableBook book = lendableBookRepository.findByFormatId(formatId);
        if (book==null) throw new LendableBookException("Lendable Book con id "+formatId+" non esiste");
        if (!book.hasOption(optionId)) {
            throw new LendingOptionNotFoundException(optionId);
        }
        book.removeOption(optionId);
        lendableBookRepository.save(book);
        log.info("Opzione {} rimossa da LendableBook {}", optionId, book.getFormatId());
    }

    /**
     * Crea una copia fisica/digitale
     */
    public LendableCopy createLendableCopy(UUID lendableBookId, Integer copyNumber) {
        return new LendableCopy(lendableBookId, copyNumber);
    }

    /**
     * Controlla se disponibile per l’utente
     */
    public boolean isAvailableForUser(LendableBook book, UserSubscriptionData subscription) {
        return book.isAvailableForUser(subscription);
    }

    public List<LendableBook> getLendableFormatsForBook(UUID bookId) {
        List<LendableBook> allLendableBooksForBook = lendableBookRepository.findAllByBookId(bookId);
        if (allLendableBooksForBook.isEmpty()) {throw new LendableBookException("Nessun formato prestabile per il libro indicato");}
        return allLendableBooksForBook;
    }

    public Set<LendingOption> getOptionsForLendableFormat(UUID formatId) {
        LendableBook book = lendableBookRepository.findByFormatId(formatId);
        if(book==null) throw new LendableBookException("Lendable Book con id "+formatId+" non esiste");
        return book.getOptions();
    }
}
