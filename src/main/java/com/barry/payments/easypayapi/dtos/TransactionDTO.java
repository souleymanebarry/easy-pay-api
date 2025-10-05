package com.barry.payments.easypayapi.dtos;

import com.barry.payments.easypayapi.models.enums.PaymentType;
import com.barry.payments.easypayapi.models.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TransactionDTO {

    private String id;
    private BigDecimal amount;
    private PaymentType paymentType;
    private Status status;
    private List<OrderLineDTO> orderLines;
}
