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

    public static Transaction createTransaction(int accountId, int amount) {
        return Transaction.builder()
            .transactionId(Faker.instance().number().numberBetween(1, 100))
            .accountId(accountId)
            .amount(amount)
            .isDebit(Faker.instance().bool().bool())
            .build();
    }

    public static Transaction createTransaction(int accountId, int amount, boolean isDebit) {
        return Transaction.builder()
            .transactionId(Faker.instance().number().numberBetween(1, 100))
            .accountId(accountId)
            .amount(amount)
            .isDebit(isDebit)
            .build();
    }

}

