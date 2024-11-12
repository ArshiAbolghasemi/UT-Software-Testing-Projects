package domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;

import util.TransactionFaker;

public class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() { transaction = TransactionFaker.createTransaction(); }

    @AfterEach
    void tearDown() { transaction = null; }

    @Test
    void equals_shouldReturnTrue_whenTransactionIsSame() {
        assertTrue(transaction.equals(transaction));
    }

    @Test
    void equals_shouldReturnFale_whenTransactionIsNotSame() {
        Transaction anotherTransaction = TransactionFaker.createTransaction();
        anotherTransaction.setTransactionId(anotherTransaction.getTransactionId() + 1);
        assertFalse(transaction.equals(anotherTransaction));
    }

    @Test
    void equalt_shouldReturnFalse_whenObjectIsNotInstanceOfTransaction() {
        assertFalse(transaction.equals(Faker.instance().animal()));
    }
}
