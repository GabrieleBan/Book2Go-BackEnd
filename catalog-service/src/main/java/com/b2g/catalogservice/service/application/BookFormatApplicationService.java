package com.b2g.catalogservice.service.application;

import com.b2g.catalogservice.dto.BookFormatCreateDTO;
import com.b2g.catalogservice.exceptions.BookFormatNotFoundException;
import com.b2g.catalogservice.model.Entities.BookFormat;
import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.catalogservice.model.VO.FormatType;
import com.b2g.catalogservice.model.VO.Price;
import com.b2g.catalogservice.repository.BookFormatRepository;
import com.b2g.catalogservice.service.domain.BookFormatDomainService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookFormatApplicationService {

    private final BookFormatRepository bookFormatRepository;
    private final CatalogBookApplicationService catalogBookApplicationService;
    private final BookFormatDomainService bookFormatDomainService;

    @Transactional
    public BookFormat createBookFormat(UUID bookId, BookFormatCreateDTO dto) {

        CatalogBook catalogBook = catalogBookApplicationService.getCatalogBookById(bookId);


        Price price = new Price(dto.purchasePrice(), dto.discountPercentage());

        BookFormat format = bookFormatDomainService.createBookFormat(
                catalogBook,
                FormatType.valueOf(dto.formatType().toUpperCase()),
                price
        );

        return bookFormatRepository.save(format);
    }


    public List<BookFormat> getBookFormats(UUID bookId) {
        return bookFormatRepository.findByBookId(bookId);
    }

    public BookFormat getBookFormat(UUID formatId) {
        BookFormat format= bookFormatRepository.findById(formatId).orElse(null);
        if(format==null)
            throw new BookFormatNotFoundException("Format not found");
        return format;

    }
}