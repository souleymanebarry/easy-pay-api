package com.barry.payments.easypayapi.controllers.impl;


import com.barry.payments.easypayapi.dtos.OrderLineDTO;
import com.barry.payments.easypayapi.dtos.TransactionDTO;
import com.barry.payments.easypayapi.models.OrderLine;
import com.barry.payments.easypayapi.models.Transaction;
import com.barry.payments.easypayapi.models.enums.Status;
import com.barry.payments.easypayapi.reporitories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static com.barry.payments.easypayapi.models.enums.PaymentType.CREDIT_CARD;
import static com.barry.payments.easypayapi.models.enums.PaymentType.GIFT_CARD;
import static com.barry.payments.easypayapi.models.enums.PaymentType.PAYPAL;
import static com.barry.payments.easypayapi.models.enums.Status.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TransactionControllerImplIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDb() {
        transactionRepository.deleteAll().block();
    }

    @Test
    void shouldReturnOKWhenFindAllTransactions() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("OL-1")
                .productName("Samsung X190")
                .quantity(1)
                .price(BigDecimal.valueOf(123))
                .build();


        Transaction tx1 = Transaction.builder()
                .id("TX-1")
                .status(NEW)
                .amount(BigDecimal.valueOf(1000))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine))
                .build();

        Transaction tx2 = Transaction.builder()
                .id("TX-2")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(2000))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine))
                .build();


        transactionRepository.saveAll(List.of(tx1, tx2)).collectList().block();

        //when then
        webTestClient.get()
                .uri("/api/v1/transactions")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(TransactionDTO.class)
                .hasSize(2)
                .value(transactions -> assertThat(transactions).extracting("id")
                        .containsExactlyInAnyOrder("TX-1", "TX-2"));
    }


    @Test
    void shouldReturnOKWhenFindAllTransactionsWithPagination() {
        //given
        int page = 0;
        int size = 2;

        OrderLine orderLine1 = OrderLine.builder()
                .id("OL-1")
                .productName("Samsung X190")
                .quantity(1)
                .price(BigDecimal.valueOf(123))
                .build();

        OrderLine orderLine2 = OrderLine.builder()
                .id("OL-2")
                .productName("Apple MacBook Pro")
                .quantity(1)
                .price(BigDecimal.valueOf(2500))
                .build();

        Transaction tx1 = Transaction.builder()
                .id("TX-1")
                .status(NEW)
                .amount(BigDecimal.valueOf(1000))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine1))
                .build();

        Transaction tx2 = Transaction.builder()
                .id("TX-2")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(2000))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine2))
                .build();

        Transaction tx3 = Transaction.builder()
                .id("TX-3")
                .status(CAPTURED)
                .amount(BigDecimal.valueOf(3000))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine1))
                .build();


        transactionRepository.saveAll(List.of(tx1, tx2, tx3)).collectList().block();

        //when then
        webTestClient.get()
                .uri(uriBuilder ->  uriBuilder
                        .path("/api/v1/transactions")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(TransactionDTO.class)
                .hasSize(2)
                .value(transactions -> assertThat(transactions).extracting("id")
                        .containsExactlyInAnyOrder("TX-1","TX-2"));
    }

    @Test
    void shouldReturnOKWhenGettingTransactionById() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("OL-1")
                .productName("Macbook Pro")
                .quantity(1)
                .price(BigDecimal.valueOf(1500))
                .build();

        Transaction tx = Transaction.builder()
                .id("TX-3")
                .status(Status.NEW)
                .amount(BigDecimal.valueOf(1500))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine))
                .build();


        transactionRepository.save(tx).block();

        //when then
        webTestClient.get()
                .uri("/api/v1/transactions/{id}","TX-3")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .value(transaction -> {
                    assertThat(transaction.getId()).isEqualTo("TX-3");
                    assertThat(transaction.getPaymentType()).isEqualTo(CREDIT_CARD);
                    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(1500));

                });
    }

    @Test
    void shouldReturnOKWhenCreatingTransaction() {
        //given615
        TransactionDTO dto = TransactionDTO.builder()
                .id("TX-4")
                .status(Status.NEW)
                .amount(BigDecimal.valueOf(999))
                .paymentType(PAYPAL)
                .orderLines(List.of(OrderLineDTO.builder()
                        .id("OL-4")
                        .productName("iPhone 15")
                        .quantity(1)
                        .price(BigDecimal.valueOf(999))
                        .build()))
                .build();


        //when then
        webTestClient.post()
                .uri("/api/v1/transactions")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .value(saved -> {
                    assertThat(saved.getId()).isEqualTo("TX-4");
                    assertThat(saved.getStatus()).isEqualTo(NEW);
                    assertThat(saved.getPaymentType()).isEqualTo(PAYPAL);
                    assertThat(saved.getAmount()).isEqualTo(BigDecimal.valueOf(999));

                });

        assertThat(transactionRepository.findById("TX-4").block()).isNotNull();
    }


    @Test
    void shouldReturnOKWhenUpdatingTransaction() {
        // given
        OrderLine orderLine = OrderLine.builder()
                .id("OL-5")
                .productName("Gift Card")
                .quantity(1)
                .price(BigDecimal.valueOf(50))
                .build();

        Transaction tx = Transaction.builder()
                .id("TX-5")
                .amount(BigDecimal.valueOf(50))
                .paymentType(GIFT_CARD)
                .orderLines(List.of(orderLine))
                .build();

        transactionRepository.save(tx).block();

        TransactionDTO updateDto = TransactionDTO.builder()
                .id("TX-5")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(60))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(OrderLineDTO.builder()
                        .id("OL-5")
                        .productName("Gift Card")
                        .quantity(1)
                        .price(BigDecimal.valueOf(50))
                        .build()))
                .build();

        // when & then
        webTestClient.put()
                .uri("/api/v1/transactions/{id}", "TX-5")
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .value(updated -> {
                    assertThat(updated.getAmount()).isEqualTo(BigDecimal.valueOf(60));
                    assertThat(updated.getStatus()).isEqualTo(AUTHORIZED);
                });
    }
}
