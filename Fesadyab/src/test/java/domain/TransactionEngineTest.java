package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import util.TransactionFaker;

public class TransactionEngineTest {

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
        int accountId, int averageTransactionAmmount) {
        TransactionEngine transactionEngine = (new TransactionEngine());
        transactionEngine.transactionHistory = transactionHistory;
        assertEquals(transactionEngine.getAverageTransactionAmountByAccount(accountId), averageTransactionAmmount);
    }

}
