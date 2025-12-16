package com.b2g.lendservice.service.infrastructure;


import com.b2g.lendservice.model.LendableCopy;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryClient {

    @Value("${inventoryService.internal.url}")
    private String inventoryUrl;

    private final WebClient webClient = WebClient.create();

    public void retrieveCopy(LendableCopy copy, UUID libraryId) {

        UpdateCopyStateRequest body = new UpdateCopyStateRequest();
        body.setState("IN_USE");

        webClient.patch()
                .uri(inventoryUrl + "/libraries/{libraryId}/physical-copies/{bookId}/{copyNumber}",
                        libraryId, copy.getLendableBookId(), copy.getCopyNumber())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("Marked copy {} as IN_USE for library {}", copy.getCopyNumber(), libraryId);
    }

    @Setter
    @Getter
    @Data
    public static class UpdateCopyStateRequest {
        private String state;
    }
}
