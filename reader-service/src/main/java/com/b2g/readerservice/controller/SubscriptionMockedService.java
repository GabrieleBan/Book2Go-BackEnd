package com.b2g.readerservice.controller;

import com.b2g.commons.SubscriptionType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionMockedService {
    @GetMapping("/{readerId}")
    public ResponseEntity<?> subscriptions( @PathVariable UUID readerId) {
        log.info("Got subscriptions for reader " + readerId);
        UserSubscriptionDataDTO userSubscriptionDataDTO = new UserSubscriptionDataDTO(SubscriptionType.SUB_TIER1,2);
//        UserSubscriptionDataDTO userSubscriptionDataDTO = new UserSubscriptionDataDTO(SubscriptionType.SUB_TIER2,2);

        return ResponseEntity.ok().body(userSubscriptionDataDTO);
    }
    @Data
    private static class UserSubscriptionDataDTO {
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
