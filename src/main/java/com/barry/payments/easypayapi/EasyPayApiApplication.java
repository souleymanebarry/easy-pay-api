package com.barry.payments.easypayapi;

import com.barry.payments.easypayapi.models.OrderLine;
import com.barry.payments.easypayapi.models.Transaction;
import com.barry.payments.easypayapi.models.enums.PaymentType;
import com.barry.payments.easypayapi.models.enums.Status;
import com.barry.payments.easypayapi.reporitories.OrderLineRepository;
import com.barry.payments.easypayapi.reporitories.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootApplication
public class EasyPayApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyPayApiApplication.class, args);
	}



	//@Bean
	CommandLineRunner start(TransactionRepository transactionRepository,
							OrderLineRepository orderLineRepository) {
		return args -> {

			// ⚙️ Étape 1 : Nettoyage des collections
			Mono<Void> cleanup = orderLineRepository.deleteAll()
					.then(transactionRepository.deleteAll());

			// ⚙️ Étape 2 : Création de 30 transactions avec leurs OrderLines
			cleanup.thenMany(Flux.range(1, 30)
							.flatMap(i -> {

								// 🔹 Créer 1 à 3 lignes de commande
								List<OrderLine> orderLines = IntStream.range(1, 1 + (int) (Math.random() * 3))
										.mapToObj(j -> OrderLine.builder()
												.id("OL-" + i + "-" + j)
												.productName("Produit-" + i + "-" + j)
												.quantity((int) (1 + Math.random() * 5))
												.price(BigDecimal.valueOf(50 + Math.random() * 500))
												.build())
										.toList();

								// 🔹 Persister les lignes dans Mongo
								return orderLineRepository.saveAll(orderLines)
										.collectList()
										.flatMap(savedOrderLines -> {

											// Calcul automatique du montant total
											BigDecimal totalAmount = savedOrderLines.stream()
													.map(ol -> ol.getPrice().multiply(BigDecimal.valueOf(ol.getQuantity())))
													.reduce(BigDecimal.ZERO, BigDecimal::add);

											// 🔹 Créer la transaction correspondante
											Transaction transaction = Transaction.builder()
													.id("TX-" + i)
													.amount(totalAmount)
													.paymentType(PaymentType.values()[(int) (Math.random() * PaymentType.values().length)])
													.status(Status.values()[(int) (Math.random() * Status.values().length)])
													.orderLines(savedOrderLines)
													.build();

											// 🔹 Persister la transaction
											return transactionRepository.save(transaction)
													.doOnSuccess(t -> System.out.println("✅ Transaction saved: " + t.getId()));
										});
							}))
					.then()
					.subscribe(
							null,
							error -> System.err.println("❌ Error initializing data: " + error.getMessage()),
							() -> System.out.println("🚀 30 transactions + orderLines initialized successfully!")
					);
		};
	}

}
