package com.b2g.catalogservice.service.domain;

import com.b2g.catalogservice.exceptions.FormatException;
import com.b2g.catalogservice.model.Entities.BookFormat;
import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.catalogservice.model.VO.FormatType;
import com.b2g.catalogservice.model.VO.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookFormatDomainService {

    public BookFormat createBookFormat(CatalogBook catalogBook, FormatType formatType, Price price,Integer pages,String isbn ) throws FormatException {
        if (catalogBook.hasFormat(formatType)) {
            throw new FormatException("Book already has a format of type " + formatType);
        }

        return BookFormat.create(
                catalogBook.getId(),
                formatType,
                price,
                pages,
                isbn


        );
    }
}