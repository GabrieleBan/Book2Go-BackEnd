package com.b2g.inventoryservice.dto;

import com.b2g.inventoryservice.model.valueObjects.CopyCondition;
import lombok.Data;

import java.util.UUID;

@Data
public class createLibraryCopyFromStockdto {
    UUID bookId;
    UUID libraryId;
    CopyCondition copyCondition;
    String conditionDescription;
}
