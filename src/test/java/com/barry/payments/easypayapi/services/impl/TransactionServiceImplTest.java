package com.barry.payments.easypayapi.services.impl;

import com.barry.payments.easypayapi.exceptions.CannotModifyCapturedTransactionException;
import com.barry.payments.easypayapi.exceptions.CapturedNotAllowException;
import com.barry.payments.easypayapi.exceptions.InvalidPaginationParameterException;
import com.barry.payments.easypayapi.exceptions.TransactionNotFoundException;
import com.barry.payments.easypayapi.models.OrderLine;
import com.barry.payments.easypayapi.models.Transaction;

import com.barry.payments.easypayapi.reporitories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.barry.payments.easypayapi.models.enums.PaymentType.CREDIT_CARD;
import static com.barry.payments.easypayapi.models.enums.PaymentType.PAYPAL;
import static com.barry.payments.easypayapi.models.enums.Status.AUTHORIZED;
import static com.barry.payments.easypayapi.models.enums.Status.CAPTURED;
import static com.barry.payments.easypayapi.models.enums.Status.NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {


    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldThrowExceptionWhenPageOrSizeValuesIsNotValid() {
        int invalidPage = -1;
        int invalidSize = 10;

        // when then
        assertThatThrownBy(() -> transactionService.getAllTransactionsPagination(invalidPage, invalidSize).blockLast())
                .isInstanceOf(InvalidPaginationParameterException.class)
                .hasMessageContaining(
                        "Invalid pagination parameters: page= "+invalidPage+", size= "+invalidSize+
                                " : { Page must be >= 0 and/Or size must be > 0 }"
                );


        // verify
        verify(transactionRepository, never()).findAll();
    }


    @Test
    void shouldReturnAllTransactionsWhenPaginationIsValidAndRepositorySucceeds() {
        //given
        int page = 0;
        int size = 5;
        OrderLine orderLine1 = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12))
                .build();

        Transaction tx1 = Transaction.builder()
                .id("TX-1")
                .status(NEW)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine1))
                .build();

        OrderLine orderLine2 = OrderLine.builder()
                .id("LX-2")
                .productName("Smart phone")
                .quantity(1)
                .price(BigDecimal.valueOf(5020))
                .build();

        Transaction tx2 = Transaction.builder()
                .id("TX-2")
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(5020))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine2))
                .build();

        when(transactionRepository.findAll()).thenReturn(Flux.just(tx1, tx2));

        //when

        Flux<Transaction> result = transactionService.getAllTransactionsPagination(page, size);

        StepVerifier.create(result)
                .expectNext(tx1)
                .expectNext(tx2)
                .verifyComplete();

        // verify
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnAllTransactionsWhenRepositoryIsSucceeds() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(NEW)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        OrderLine orderLine2 = OrderLine.builder()
                .id("LX-1")
                .productName("Smart phone")
                .quantity(1)
                .price(BigDecimal.valueOf(5020)).build();

        Transaction transaction2 = Transaction.builder()
                .id(transactionID)
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine2)).build();

        when(transactionRepository.findAll()).thenReturn(Flux.just(transaction, transaction2));

        //when
        Flux<Transaction> result = transactionService.getAllTransactions();

        //then
        StepVerifier.create(result)
                .expectNext(transaction)
                .expectNext(transaction2)
                .verifyComplete();



        //verify
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnTransactionWhenTransactionIdIsValid() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(NEW)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transaction));

        //when
        Mono<Transaction> result = transactionService.getTransactionById(transactionID);

        //then
        StepVerifier.create(result)
                .assertNext(fetchedTransaction -> {
                    assertThat(fetchedTransaction.getId()).isEqualTo(transaction.getId());
                    assertThat(fetchedTransaction.getStatus()).isEqualTo(NEW);
                    assertThat(fetchedTransaction.getAmount()).isEqualTo(transaction.getAmount());
                    assertThat(fetchedTransaction.getPaymentType()).isEqualTo(transaction.getPaymentType());
                    assertThat(fetchedTransaction.getOrderLines()).hasSameElementsAs(transaction.getOrderLines());
                }).verifyComplete();


        //verify
        verify(transactionRepository, times(1)).findById(anyString());
    }


    @Test
    void shouldThrowExceptionTransactionIdIsNotValid() {
        //given
        String transactionID = "AXA";

        when(transactionRepository.findById(anyString())).thenReturn(Mono.empty());

        //when then
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionID).block())
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with ID: " + transactionID);

        //verify
        verify(transactionRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldCreateTransactionWhenAllFieldsAreValid() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(NEW)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        //when
        Mono<Transaction> result = transactionService.createTransaction(transaction);

        //then
        StepVerifier.create(result)
                .assertNext(savedTransaction -> {
                    assertThat(savedTransaction.getId()).isEqualTo(transaction.getId());
                    assertThat(savedTransaction.getStatus()).isEqualTo(NEW);
                    assertThat(savedTransaction.getAmount()).isEqualTo(transaction.getAmount());
                    assertThat(savedTransaction.getPaymentType()).isEqualTo(transaction.getPaymentType());
                    assertThat(savedTransaction.getOrderLines()).hasSameElementsAs(transaction.getOrderLines());
                }).verifyComplete();


        //verify
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTransactionWithCapturedStatus() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(CAPTURED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        Transaction transactionToUpdate = Transaction.builder()
                .id(transactionID)
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transaction));

        //when then
        assertThatThrownBy(()->transactionService.updateTransaction(transactionID, transactionToUpdate).block())
                .isInstanceOf(CannotModifyCapturedTransactionException.class)
                .hasMessageContaining("Cannot modify CAPTURED transaction");

        //verify
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


    @Test
    void shouldThrowExceptionWhenUpdatingTransactionWithNewOrderLine() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(NEW)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        OrderLine orderLine2 = OrderLine.builder()
                .id("LX-1")
                .productName("Smart phone")
                .quantity(1)
                .price(BigDecimal.valueOf(5020)).build();

        Transaction transactionToUpdate = Transaction.builder()
                .id(transactionID)
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine2)).build();

        when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transaction));

        //when then
        assertThatThrownBy(()->transactionService.updateTransaction(transactionID, transactionToUpdate).block())
                .isInstanceOf(CannotModifyCapturedTransactionException.class)
                .hasMessageContaining("The order of transaction cannot be changed");

        //verify
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


    @Test
    void shouldThrowExceptionWhenUpdatingStatusToCapturedIfNotAuthorized() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(NEW)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();


        Transaction transactionToUpdate = Transaction.builder()
                .id(transactionID)
                .status(CAPTURED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transaction));

        //when then
        assertThatThrownBy(()->transactionService.updateTransaction(transactionID, transactionToUpdate).block())
                .isInstanceOf(CapturedNotAllowException.class)
                .hasMessageContaining("Cannot switch to CAPTURED if the transaction is not AUTHORIZED");

        //verify
        verify(transactionRepository, never()).save(any(Transaction.class));

    }


    @Test
    void shouldUpdatingTransactionWhenStatusChangesFromAuthorizedToCaptured() {
        //given
        OrderLine orderLine = OrderLine.builder()
                .id("LX-1")
                .productName("Ski Gloves")
                .quantity(2)
                .price(BigDecimal.valueOf(12)).build();

        String transactionID = "AXA";
        Transaction transaction = Transaction.builder()
                .id(transactionID)
                .status(AUTHORIZED)
                .amount(BigDecimal.valueOf(16646))
                .paymentType(CREDIT_CARD)
                .orderLines(List.of(orderLine)).build();

        Transaction transactionToUpdate = Transaction.builder()
                .id(transactionID)
                .status(CAPTURED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine)).build();


        Transaction updatedTransaction = Transaction.builder()
                .id(transactionID)
                .status(CAPTURED)
                .amount(BigDecimal.valueOf(43))
                .paymentType(PAYPAL)
                .orderLines(List.of(orderLine)).build();


        when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transaction));
        when(transactionRepository.save(any())).thenReturn(Mono.just(transactionToUpdate));

        //when
        Mono<Transaction> result = transactionService.updateTransaction(transactionID, transactionToUpdate);

        StepVerifier.create(result)
                .assertNext(updatedTransactionValue -> {
                    assertThat(updatedTransactionValue.getStatus()).isEqualTo(updatedTransaction.getStatus());
                    assertThat(updatedTransactionValue.getAmount()).isEqualTo(updatedTransaction.getAmount());
                    assertThat(updatedTransactionValue.getPaymentType()).isEqualTo(updatedTransaction.getPaymentType());
                }).verifyComplete();

        //verify
        verify(transactionRepository, times(1)).findById(anyString());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
