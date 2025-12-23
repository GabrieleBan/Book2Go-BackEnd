package com.b2g.inventoryservice.service.domainService;

import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.repository.LibraryCopyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final LibraryCopyRepository copyRepository;

    public void markCopyInUse(LibraryCopy copy) {
        if (!copy.getCondition().isUsable()) {
            throw new IllegalStateException("Copy is not usable due to condition: " + copy.getCondition());
        }
        AvailabilityState state=copy.getUseState();
        if (state != AvailabilityState.FREE && state!=AvailabilityState.RESERVED) {
            throw new IllegalStateException("Copy is not free, current state: " + copy.getUseState());
        }

        copy.markInUse();
    }


}