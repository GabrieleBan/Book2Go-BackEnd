package com.b2g.notificationservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    @GetMapping({"", "/"})
    public ResponseEntity<?> helloWorld() {
        return ResponseEntity.ok("hello world from notification service");
    }
}
