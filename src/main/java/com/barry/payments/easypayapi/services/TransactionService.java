package com.barry.payments.easypayapi.services;

import com.barry.payments.easypayapi.models.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {


    Flux<Transaction> getAllTransactions();

    Flux<Transaction> getAllTransactionsPagination(int page, int size);

    Mono<Transaction> getTransactionById(String id);

    Mono<Transaction> createTransaction(Transaction transaction);

    Mono<Transaction> updateTransaction(String id, Transaction transaction);
}
