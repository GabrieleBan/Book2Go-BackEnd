package com.b2g.lendservice.service.infrastructure;

import com.b2g.commons.FormatType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.NoSuchElementException;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class CatalogACLClient {

    private final RestTemplate restTemplate;

    @Value("${catalogService.internal.url}")
    private String catalogServiceUrl; // es. http://catalog-service

    /**
     * Recupera un formato dal Catalog e restituisce solo i dati rilevanti per Lend.
     */
    public CatalogFormatResponse getBookFormat(UUID formatId) {
        String url = catalogServiceUrl + "/formats/" + formatId;

        ResponseEntity<CatalogRawFormatResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, CatalogRawFormatResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new NoSuchElementException("Catalog returned empty for formatId " + formatId);
        }

        CatalogRawFormatResponse catalogFormat = response.getBody();

        // Traduzione DTO Catalog -> VO/dominio Lend
        return new CatalogFormatResponse(
                catalogFormat.bookId(),
                catalogFormat.id(),
                translateFormatType(catalogFormat.formatType())
        );
    }

    private FormatType translateFormatType(String catalogFormatType) {
        return switch (catalogFormatType) {
            case "EBOOK" -> FormatType.EBOOK;
            case "AUDIOBOOK" -> FormatType.AUDIOBOOK;
            default -> FormatType.PHYSICAL;
        };
    }

    /**
     * DTO intermedio usato solo dall'ACL client per deserializzare il JSON di Catalog.
     */
    public record CatalogRawFormatResponse(UUID id, String formatType, UUID bookId) {}


    public record CatalogFormatResponse(UUID bookId, UUID formatId, FormatType formatType) {}
}