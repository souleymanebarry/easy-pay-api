package com.barry.payments.easypayapi.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderLine {

    private String id;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
