package com.barry.payments.easypayapi.controllers.impl;

import com.barry.payments.easypayapi.controllers.TransactionController;
import com.barry.payments.easypayapi.dtos.TransactionDTO;
import com.barry.payments.easypayapi.mappers.TransactionMapper;
import com.barry.payments.easypayapi.models.Transaction;
import com.barry.payments.easypayapi.services.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Log4j2
public class TransactionControllerImpl implements TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionControllerImpl(TransactionService transactionService,
                                     TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Flux<TransactionDTO> getAllTransactions() {
        log.info("GET /api/v1/transactions/withoutPagination");
        return transactionService.getAllTransactions()
                .map(transactionMapper::transactionToTransactionDto);
    }

    @Override
    public Flux<TransactionDTO> getPaginatedTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/transactions?page={}&size={}", page, size);
        return transactionService.getAllTransactionsPagination(page,size)
                .map(transactionMapper::transactionToTransactionDto);
    }

    @Override
    public Mono<TransactionDTO> getTransactionById(String id) {
        log.info("GET /api/v1/transactions/{}", id);
        return transactionService.getTransactionById(id)
                .map(transactionMapper::transactionToTransactionDto);
    }

    @Override
    public Mono<TransactionDTO> createTransaction(TransactionDTO transactionDto) {
        log.info("POST /api/v1/transactions");
        Transaction transaction = transactionMapper.transactionDtoToTransaction(transactionDto);
        return transactionService.createTransaction(transaction)
                .map(transactionMapper::transactionToTransactionDto);
    }

    @Override
    public Mono<TransactionDTO> updateTransaction(String id, TransactionDTO transactionDto) {
        log.info("PUT /api/v1/transactions/{}", id);
        Transaction transaction = transactionMapper.transactionDtoToTransaction(transactionDto);
        return transactionService.updateTransaction(id,transaction)
                .map(transactionMapper::transactionToTransactionDto);
    }
}
