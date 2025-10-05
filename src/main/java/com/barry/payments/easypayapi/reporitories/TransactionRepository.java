package com.barry.payments.easypayapi.reporitories;

import com.barry.payments.easypayapi.models.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
}
