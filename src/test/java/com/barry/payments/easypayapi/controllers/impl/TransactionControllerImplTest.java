package com.barry.payments.easypayapi.controllers.impl;


import com.barry.payments.easypayapi.dtos.OrderLineDTO;
import com.barry.payments.easypayapi.dtos.TransactionDTO;
import com.barry.payments.easypayapi.mappers.TransactionMapper;
import com.barry.payments.easypayapi.models.OrderLine;
import com.barry.payments.easypayapi.models.Transaction;
import com.barry.payments.easypayapi.services.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static com.barry.payments.easypayapi.models.enums.PaymentType.PAYPAL;
import static com.barry.payments.easypayapi.models.enums.Status.AUTHORIZED;
import static com.barry.payments.easypayapi.models.enums.Status.NEW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TransactionControllerImpl.class)
class TransactionControllerImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionMapper transactionMapper;


    @Test
    void shouldReturnOKWhenWhenFindAllTransactions() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction = Transaction.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine))
                .build();

        OrderLine orderLine2 = OrderLine.builder()
                .id("TX-3")
                .productName("APPLE MACBOOK PRO-X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction2 = Transaction.builder()
                .id("AXA")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine2))
                .build();

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .build();

        TransactionDTO transactionDTO2 = TransactionDTO.builder()
                .id("AXA")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .build();
        when(transactionService.getAllTransactions()).thenReturn(Flux.just(transaction, transaction2));
        when(transactionMapper.transactionToTransactionDto(transaction)).thenReturn(transactionDTO);
        when(transactionMapper.transactionToTransactionDto(transaction2)).thenReturn(transactionDTO2);


        //when then
        webTestClient.get()
                .uri("/api/v1/transactions/withoutPagination")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(TransactionDTO.class)
                .hasSize(2)
                .contains(transactionDTO, transactionDTO2);

        //verify
        verify(transactionService, times(1)).getAllTransactions();
        verify(transactionMapper, times(2)).transactionToTransactionDto(any(Transaction.class));
    }

    @Test
    void shouldReturnOKWhenWhenFindAllTransactionsWithPagination() {
        //given
        int page = 0;
        int size = 5;
        OrderLine orderLine = OrderLine.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction = Transaction.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine))
                .build();

        OrderLine orderLine2 = OrderLine.builder()
                .id("TX-3")
                .productName("APPLE MACBOOK PRO-X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction2 = Transaction.builder()
                .id("AXA")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine2))
                .build();

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .build();

        TransactionDTO transactionDTO2 = TransactionDTO.builder()
                .id("AXA")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .build();
        when(transactionService.getAllTransactionsPagination(page, size)).thenReturn(Flux.just(transaction));
        // page=0, size=1 => only the first page

        when(transactionMapper.transactionToTransactionDto(transaction)).thenReturn(transactionDTO);
        when(transactionMapper.transactionToTransactionDto(transaction2)).thenReturn(transactionDTO2);


        //when then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/transactions")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(TransactionDTO.class)
                .hasSize(1)
                .contains(transactionDTO);

        //verify
        verify(transactionService, times(1)).getAllTransactionsPagination(page, size);
        verify(transactionMapper, times(1)).transactionToTransactionDto(any(Transaction.class));
    }


    @Test
    void shouldReturnOKWhenWhenGettingTransactionById() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction = Transaction.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine))
                .build();

        OrderLineDTO orderLineDTO = OrderLineDTO.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();


        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLineDTO))
                .build();

        when(transactionService.getTransactionById(anyString())).thenReturn(Mono.just(transaction));
        when(transactionMapper.transactionToTransactionDto(transaction)).thenReturn(transactionDTO);


        //when then
        webTestClient.get()
                .uri("/api/v1/transactions/AWS")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .isEqualTo(transactionDTO);

        //verify
        verify(transactionService, times(1)).getTransactionById(anyString());
        verify(transactionMapper, times(1)).transactionToTransactionDto(any(Transaction.class));
    }


    @Test
    void shouldReturnOKWhenCreatingNewTransaction() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction = Transaction.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine))
                .build();

        OrderLineDTO orderLineDTO = OrderLineDTO.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();


        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLineDTO))
                .build();

        when(transactionService.createTransaction(transaction)).thenReturn(Mono.just(transaction));
        when(transactionMapper.transactionDtoToTransaction(transactionDTO)).thenReturn(transaction);
        when(transactionMapper.transactionToTransactionDto(transaction)).thenReturn(transactionDTO);


        //when then
        webTestClient.post()
                .uri("/api/v1/transactions")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .isEqualTo(transactionDTO);

        //verify
        verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        verify(transactionMapper, times(1)).transactionDtoToTransaction(any(TransactionDTO.class));
        verify(transactionMapper, times(1)).transactionToTransactionDto(any(Transaction.class));
    }

    @Test
    void shouldReturnOKWhenUpdatingNewTransaction() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();

        Transaction transaction = Transaction.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine))
                .build();

        OrderLineDTO orderLineDTO = OrderLineDTO.builder()
                .id("TX-2")
                .productName("SAMSUNG X190")
                .price(BigDecimal.valueOf(123))
                .quantity(1).build();


        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id("AWS")
                .status(NEW)
                .amount(BigDecimal.valueOf(7687569))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLineDTO))
                .build();

        when(transactionService.updateTransaction(eq("AWS"), any(Transaction.class))).thenReturn(Mono.just(transaction));
        when(transactionMapper.transactionDtoToTransaction(transactionDTO)).thenReturn(transaction);
        when(transactionMapper.transactionToTransactionDto(transaction)).thenReturn(transactionDTO);


        //when then
        webTestClient.put()
                .uri("/api/v1/transactions/AWS")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .isEqualTo(transactionDTO);

        //verify
        verify(transactionService, times(1)).updateTransaction(anyString(), any(Transaction.class));
        verify(transactionMapper, times(1)).transactionDtoToTransaction(any(TransactionDTO.class));
        verify(transactionMapper, times(1)).transactionToTransactionDto(any(Transaction.class));
    }

}
