package com.b2g.lendservice.service.infrastructure;

import com.b2g.commons.SubscriptionType;
import com.b2g.lendservice.Exceptions.InfrastructureException;
import com.b2g.lendservice.Exceptions.UserNotFoundException;
import com.b2g.lendservice.model.vo.UserSubscriptionData;
import lombok.Data;
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
        log.info("getUserSubscriptionData: " + url);
        try {
            ResponseEntity<UserSubscriptionDataDTO> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            UserSubscriptionDataDTO.class
                    );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("getUserSubscriptionData: " + response.getBody());
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
    @Data
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
