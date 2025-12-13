package com.b2g.lendservice.service;

import com.b2g.commons.FormatType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.NoSuchElementException;
import java.util.UUID;

import static com.b2g.commons.FormatType.AUDIOBOOK;
import static com.b2g.commons.FormatType.EBOOK;

@Service
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${catalogService.internal.url}")
    private String catalogServiceUrl; // es. http://catalog-service

    public CatalogFormatResponse getBookFormat(UUID bookId, UUID formatId) throws Exception {
        String url = catalogServiceUrl + "/books/" + bookId + "/format/" + formatId;
        try {
            ResponseEntity<CatalogFormatResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, CatalogFormatResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new NoSuchElementException("Catalog returned empty for bookId " + bookId + " and formatId " + formatId);
            }

        } catch (Exception e) {
            throw new Exception("Error calling Catalog service: " + e.getMessage(), e);
        }
    }

    public record CatalogFormatResponse(UUID bookId, UUID formatId, String formatType) {
        public FormatType getLendingFormat() {
        return switch (formatType) {
            case "EBOOK" -> FormatType.EBOOK;
            case "AUDIOBOOK" -> FormatType.AUDIOBOOK;
            default -> FormatType.PHYSICAL;
        };
        }
    }
}