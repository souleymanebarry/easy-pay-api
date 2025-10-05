package com.barry.payments.easypayapi.mappers;


import com.barry.payments.easypayapi.dtos.OrderLineDTO;
import com.barry.payments.easypayapi.dtos.TransactionDTO;
import com.barry.payments.easypayapi.models.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OrderLineDTO.class})
public interface TransactionMapper {

    Transaction transactionDtoToTransaction(TransactionDTO transactionDto);

    TransactionDTO transactionToTransactionDto(Transaction transaction);

}
