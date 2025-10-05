package com.barry.payments.easypayapi.reporitories;

import com.barry.payments.easypayapi.models.OrderLine;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OrderLineRepository extends ReactiveMongoRepository<OrderLine, String> {
}
