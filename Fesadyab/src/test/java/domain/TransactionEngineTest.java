package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import util.TransactionFaker;

public class TransactionEngineTest {

    private TransactionEngine createTransactionEngine(ArrayList<Transaction> transactionHistory) {
        TransactionEngine transactionEngine = new TransactionEngine();
        transactionEngine.transactionHistory = transactionHistory;
        return transactionEngine;
    }

    static Object[][] transactionAverageAmountProvider() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
            TransactionFaker.createTransaction(1, 10_000),
            TransactionFaker.createTransaction(2, 25_000),
            TransactionFaker.createTransaction(1, 20_000)
        ));

        return new Object[][] {
            { transactionHistory, 1, 15_000 },
            { transactionHistory, 3, 0 }
        };
    }
    @ParameterizedTest
    @MethodSource("transactionAverageAmountProvider")
    @DisplayName("should return correct average transaction amount for account")
    void shouldReturnCorrectAverageTransactionAmmountForAccount(ArrayList<Transaction> transactionHistory,
        int accountId, int expectedAverageTransactionAmmount) {
        TransactionEngine transactionEngine = createTransactionEngine(transactionHistory);
        assertEquals(expectedAverageTransactionAmmount, transactionEngine.getAverageTransactionAmountByAccount(accountId));
    }

    static Object[][] transactionPatternAboveThresholdProvider() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
            TransactionFaker.createTransaction(10_000),
            TransactionFaker.createTransaction(12_500),
            TransactionFaker.createTransaction(15_000),
            TransactionFaker.createTransaction(20_000),
            TransactionFaker.createTransaction(30_000)
        ));

        return new Object[][] {
            { new ArrayList<>(), 4_000, 0 },
            { transactionHistory, 50_000, 0 },
            { transactionHistory, 15_000, 10_000 },
            { transactionHistory, 10_000, 0 },
        };
    }
    @ParameterizedTest
    @MethodSource("transactionPatternAboveThresholdProvider")
    @DisplayName("should return correct transaction pattern above threshold that is given")
    void shouldReturnCorrectTransactionPatternAboveThreshold_whenThresholdIsGiven(
        ArrayList<Transaction> transactionHistory, int threshold, int expectedTransactionPatternAboveThreshold) {
        TransactionEngine transactionEngine = createTransactionEngine(transactionHistory);
        assertEquals(expectedTransactionPatternAboveThreshold,
            transactionEngine.getTransactionPatternAboveThreshold(threshold));
    }

    static Object[][] transactionFraudScoreProvider() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
            TransactionFaker.createTransaction(1, 10_000),
            TransactionFaker.createTransaction(1, 5_000),
            TransactionFaker.createTransaction(1, 15_000)
        ));

        return new Object[][] {
            { transactionHistory, TransactionFaker.createTransaction(1, 12_000, false), 0 },
            { transactionHistory, TransactionFaker.createTransaction(1, 5_000, true), 0 },
            { transactionHistory, TransactionFaker.createTransaction(1, 35_000, true), 15_000 },
        };
    }
    @ParameterizedTest
    @MethodSource("transactionFraudScoreProvider")
    @DisplayName("should return correct fraud score for transaction")
    void shouldReturnCorrectFraudScoreForTransaction(ArrayList<Transaction> transactionHistory, Transaction transaction,
        int expectedFraudScore) {
        TransactionEngine transactionEngine = createTransactionEngine(transactionHistory);
        assertEquals(expectedFraudScore, transactionEngine.detectFraudulentTransaction(transaction));
    }

    static Object[][] addTransactionProvider() {
        Transaction existedTransaction = TransactionFaker.createTransaction(1, 10_000, true);
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
            existedTransaction,
            TransactionFaker.createTransaction(1, 1_500),
            TransactionFaker.createTransaction(1, 500)
        ));

        return new Object[][] {
            { transactionHistory, existedTransaction, 0 },
            { transactionHistory, TransactionFaker.createTransaction(1, 20_000, true), 12_000 },
            { transactionHistory, TransactionFaker.createTransaction(1, 7_000, true), 0 },
            {
                new ArrayList<>(List.of(
                    TransactionFaker.createTransaction(1, 1_000),
                    TransactionFaker.createTransaction(1, 2_000)
                )),
                TransactionFaker.createTransaction(1, 3_000, false),
                1_000
            }
        };
    }
    @ParameterizedTest
    @MethodSource("addTransactionProvider")
    @DisplayName("should add transaction correctly and detect fraud if is existed")
    void shouldAddTransactionCorrectlyAndDetectFraudIfExisted(ArrayList<Transaction> transactionHistory,
        Transaction transaction, int expectedFraudScore) {
        TransactionEngine transactionEngine = createTransactionEngine(transactionHistory);
        assertEquals(expectedFraudScore, transactionEngine.addTransactionAndDetectFraud(transaction));
    }

}
