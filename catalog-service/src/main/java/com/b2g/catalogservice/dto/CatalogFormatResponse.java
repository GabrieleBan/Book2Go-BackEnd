package com.b2g.catalogservice.dto;


import com.b2g.catalogservice.model.VO.FormatType;

import java.util.UUID;

public record CatalogFormatResponse(
        UUID bookId,
        UUID formatId,
        FormatType formatType
) {}
