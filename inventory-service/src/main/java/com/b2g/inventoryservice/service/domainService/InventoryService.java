package com.b2g.inventoryservice.service.domainService;

import com.b2g.inventoryservice.exceptions.StockException;
import com.b2g.inventoryservice.model.entities.Library;
import com.b2g.inventoryservice.model.entities.ReferenceBook;
import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.entities.RetailStock;
import com.b2g.inventoryservice.model.valueObjects.StockAvailabilityStatus;
import com.b2g.inventoryservice.model.valueObjects.StockId;
import com.b2g.inventoryservice.repository.LibraryCopyRepository;
import com.b2g.inventoryservice.repository.ReferenceBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final LibraryCopyRepository copyRepository;
    private final ReferenceBookRepository referenceBookRepository;

    public void addLibraryCopy(LibraryCopy libraryCopy) {}

    public boolean isAvailable(RetailStock stock) {
        return stock.isAvailable();
    }

    public Integer calculateTotalBookStockInLibraries(List<RetailStock> stocks) {
        Integer totalBookStock;
        if(stocks==null || stocks.isEmpty()) {
            totalBookStock = null;
        }
        else {
            totalBookStock=stocks.stream().mapToInt(RetailStock::getQuantity).sum();
        }
        return totalBookStock;
    }

    public boolean changeBookAvailability(ReferenceBook book, List<RetailStock> stocks) {

        Integer total=calculateTotalBookStockInLibraries(stocks);
        return book.updateAvailabilityBasedOnTotalStock(total);
    }

    public int changeStockQuantity(RetailStock stock, int quantity) {
        if(quantity==0) {
            return stock.getQuantity();
        }
        if(quantity>0) {
            return stock.increase(quantity);
        }
        else
            return stock.decrease(-quantity);

    }

    public RetailStock initLibraryStock(ReferenceBook book, Library lib, int quantity) {
        if(book==null || lib==null) {
            throw new StockException("Il libro richiesto non viene gestito da questa libreria");
        }
        return RetailStock.initializeStock(book,lib,quantity);
    }
}