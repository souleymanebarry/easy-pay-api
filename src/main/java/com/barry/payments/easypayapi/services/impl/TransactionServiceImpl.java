package com.barry.payments.easypayapi.services.impl;

import com.barry.payments.easypayapi.exceptions.CannotModifyCapturedTransactionException;
import com.barry.payments.easypayapi.exceptions.CapturedNotAllowException;
import com.barry.payments.easypayapi.exceptions.InvalidPaginationParameterException;
import com.barry.payments.easypayapi.exceptions.TransactionNotFoundException;
import com.barry.payments.easypayapi.models.Transaction;
import com.barry.payments.easypayapi.models.enums.Status;
import com.barry.payments.easypayapi.reporitories.TransactionRepository;
import com.barry.payments.easypayapi.services.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.barry.payments.easypayapi.models.enums.Status.AUTHORIZED;
import static com.barry.payments.easypayapi.models.enums.Status.CAPTURED;


@Service
@Log4j2
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Flux<Transaction> getAllTransactions() {
        return transactionRepository.findAll()
                .doOnError(e-> log.error("fail retrieve transactions from MongoDB", e))
                .doOnComplete(() -> log.info("All transactions retrieved successfully"));
    }

    @Override
    public Flux<Transaction> getAllTransactionsPagination(int page, int size) {
        if (page < 0 || size <= 0) {
            return Flux.error(new InvalidPaginationParameterException(page, size));
        }
        long skip = (long) page * size;

        return transactionRepository.findAll()
                .skip(skip) // ⬅️ Ignorer les éléments des pages précédentes
                .take(size)  // ⬅️ Récupérer "size" éléments
                .doOnSubscribe(s -> log.info("Fetching transactions page={} size={}", page, size))
                .doOnComplete(() -> log.info("Transactions page {} retrieved successfully", page))
                .doOnError(e -> log.error("Error fetching transactions page {}: {}", page, e.getMessage()));
    }

    @Override
    public Mono<Transaction> getTransactionById(String id) {
        return transactionRepository.findById(id)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException(id)))
                .doOnSuccess(fetchedTransaction ->
                        log.info("Transaction fetched successfully with ID: id= {}", id))
                .doOnError(ex ->
                        log.error("❌ Failed to fetch transaction with ID= {} : {}", id, ex.getMessage()));
    }

    @Override
    public Mono<Transaction> createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction)
                .doOnSuccess(savedTransaction->
                        log.info("Transaction saved successfully: id={}, amount={}",
                                savedTransaction.getId(), savedTransaction.getAmount()));
    }

    @Override
    public Mono<Transaction> updateTransaction(String id, Transaction transaction) {
        return getTransactionById(id)
                .flatMap(existingTransaction-> {

                   //pas possible de modifier le statut d'une transaction "CAPTURED"
                    if (CAPTURED.equals(existingTransaction.getStatus())) {
                        log.warn("Attempt to modify CAPTURED transaction with: ID={}", id);
                        return Mono.error(
                                new CannotModifyCapturedTransactionException("Cannot modify CAPTURED transaction"));
                    }

                    //Règle 2 : la commande d'une transaction ne peut pas être modifiée
                    if (existingTransaction.getOrderLines()!= null &&
                            !existingTransaction.getOrderLines().equals(transaction.getOrderLines())) {
                        log.warn("Attempt to modify order of transaction: id={}",id);
                        return Mono.error(new CannotModifyCapturedTransactionException
                                        ("The order of transaction cannot be changed"));
                    }

                    // - On ne peut passer à CAPTURED que si l'ancien statut n'est pas AUTHORIZED
                    Status oldStatus = existingTransaction.getStatus();
                    Status newStatus = transaction.getStatus() != null ? transaction.getStatus() : oldStatus;

                    if (newStatus == CAPTURED && oldStatus != AUTHORIZED) {
                        log.warn("Invalid transition to CAPTURED for transaction id={} (current status={})",
                                id, oldStatus);
                        return Mono.error(new CapturedNotAllowException
                                ("Cannot switch to CAPTURED if the transaction is not AUTHORIZED"));
                    }

                    // 🔹 Mise à jour des champs autorisés
                    Transaction updatedTransaction= existingTransaction.toBuilder()
                            .status(newStatus)
                            .amount(transaction.getAmount())
                            .paymentType(transaction.getPaymentType())
                            .build();

                    return transactionRepository.save(updatedTransaction)
                            .doOnSuccess(savedTransaction ->
                                    log.info("Saving updated transaction: id= {} with new values: , status= {}, amount= {}, paymentType= {}",
                                            id, newStatus, savedTransaction.getAmount(), savedTransaction.getPaymentType()));
                });
    }
}
