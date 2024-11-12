package util;

import com.github.javafaker.Faker;

import domain.Transaction;

public class TransactionFaker {

    public static Transaction createTransaction() {
        return Transaction.builder()
            .transactionId(Faker.instance().number().numberBetween(1, 100))
            .accountId(Faker.instance().number().numberBetween(1, 100))
            .amount(Faker.instance().number().numberBetween(10_000, 100_000))
            .isDebit(Faker.instance().bool().bool())
            .build();
    }
}

