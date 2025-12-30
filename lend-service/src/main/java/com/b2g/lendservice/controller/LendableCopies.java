package com.b2g.lendservice.controller;

import com.b2g.lendservice.annotation.RequireRole;
import com.b2g.lendservice.model.LendableCopy;
import com.b2g.lendservice.service.application.LendingsApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/lendable-copies")
@RequiredArgsConstructor
public class LendableCopies {

    private final LendingsApplicationService lendingsApplicationService;

    //restituisce tutte le copie che fanno parte di un lending in stato awaiting
    // è un metodo di supporto all'employee per portarsi le copie "avanti" senza doverle cercare quando arriva il lettore (risparmiare tempo)
    @RequireRole({"ADMIN","EMPLOYEE"})
    @GetMapping("/")
    @Transactional
    public ResponseEntity<List<LendableCopy>> retrieveLendableCopies(
            @RequestParam(required = false) UUID libraryId
    )  {

        List<LendableCopy> copies = lendingsApplicationService.getAllAwaitingLendCopies(libraryId);
        return ResponseEntity.status(HttpStatus.OK).body(copies);
    }

}