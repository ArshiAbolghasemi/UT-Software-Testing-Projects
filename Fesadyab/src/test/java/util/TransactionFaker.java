package util;

import java.time.Instant;

import com.github.javafaker.Faker;

import domain.Transaction;

public class TransactionFaker {

    private static long generateTransactionId() {
        Instant now = Instant.now();
        return now.getEpochSecond() * 1_000_000 + now.getNano() / 1_000;
    }

    public static Transaction createTransaction() {
        return Transaction.builder()
            .transactionId(generateTransactionId())
            .accountId(Faker.instance().number().numberBetween(1, 100))
            .amount(Faker.instance().number().numberBetween(10_000, 100_000))
            .isDebit(Faker.instance().bool().bool())
            .build();
    }

    public static Transaction createTransaction(int accountId, int amount) {
        return Transaction.builder()
            .transactionId(generateTransactionId())
            .accountId(accountId)
            .amount(amount)
            .isDebit(Faker.instance().bool().bool())
            .build();
    }

    public static Transaction createTransaction(int accountId, int amount, boolean isDebit) {
        return Transaction.builder()
            .transactionId(generateTransactionId())
            .accountId(accountId)
            .amount(amount)
            .isDebit(isDebit)
            .build();
    }

}

