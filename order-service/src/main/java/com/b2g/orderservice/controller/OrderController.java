package com.b2g.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    //Hello World Mapping for testing
    @GetMapping({"", "/"})
    public String hello() {
        return "Hello from Order Service!";
    }
}