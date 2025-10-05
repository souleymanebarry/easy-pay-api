package com.barry.payments.easypayapi.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderLineDTO {

    private String id;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
