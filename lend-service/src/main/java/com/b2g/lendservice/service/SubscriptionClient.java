package com.b2g.lendservice.service;

import com.b2g.commons.SubscriptionType;
import com.b2g.lendservice.Exceptions.InfrastructureException;
import com.b2g.lendservice.Exceptions.UserNotFoundException;
import com.b2g.lendservice.model.UserSubscriptionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import org.springframework.http.HttpMethod;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionClient {

    private final RestTemplate restTemplate;

    @Value("${subscriptionService.internal.url}")
    private String subscriptionServiceUrl;

    /**
     * Recupera i dati di sottoscrizione dell’utente.
     * Se il servizio non risponde o l’utente non esiste, lancia errore
     */
    public UserSubscriptionData getUserSubscriptionData(UUID userId) throws Exception {
        String url = subscriptionServiceUrl.replace("readerUUID", userId.toString());

        try {
            ResponseEntity<UserSubscriptionDataDTO> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            UserSubscriptionDataDTO.class
                    );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserSubscriptionDataDTO dto = response.getBody();
                return new UserSubscriptionData(dto.tier(), dto.maxConcurrentLends());
            } else {
                log.warn("User not found or service unavailable");
               throw new UserNotFoundException("User Id non trovato");
            }

        } catch (Exception e) {
            log.error("Error retrieving subscription for user {}", userId, e);
            throw  new InfrastructureException("Servizio abbonamenti non risponde");
        }
    }

    // DTO di trasferimento tra microservizi
    public static class UserSubscriptionDataDTO {
        private SubscriptionType tier;
        private Integer maxConcurrentLends;

        public UserSubscriptionDataDTO() {}

        public UserSubscriptionDataDTO(SubscriptionType tier, Integer maxConcurrentLends) {
            this.tier = tier;
            this.maxConcurrentLends = maxConcurrentLends;
        }

        public SubscriptionType tier() { return tier; }
        public Integer maxConcurrentLends() { return maxConcurrentLends; }
    }
}
