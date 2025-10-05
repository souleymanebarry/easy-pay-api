package com.barry.payments.easypayapi.controllers;


import com.barry.payments.easypayapi.dtos.TransactionDTO;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequestMapping("api/v1/transactions")
public interface TransactionController {


    @GetMapping("/withoutPagination")
    Flux<TransactionDTO> getAllTransactions();

    @GetMapping
    Flux<TransactionDTO> getPaginatedTransactions(
            @RequestParam(defaultValue = "O") int page,
            @RequestParam(defaultValue = "5") int size
    );


    @GetMapping("/{id}")
    Mono<TransactionDTO> getTransactionById(@PathVariable String id);

    @PostMapping
    Mono<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDto);


    @PutMapping("/{id}")
    Mono<TransactionDTO> updateTransaction(@PathVariable String id, @RequestBody TransactionDTO transactionDto);
}
