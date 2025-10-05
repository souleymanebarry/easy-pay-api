package com.barry.payments.easypayapi.models;


import com.barry.payments.easypayapi.models.enums.PaymentType;
import com.barry.payments.easypayapi.models.enums.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.barry.payments.easypayapi.models.enums.Status.NEW;


@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Transaction {

    @Id
    private String id;
    private BigDecimal amount;
    private PaymentType paymentType;
    @Builder.Default
    private Status status = NEW;
    @Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();
}
