package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Transaction {
    long transactionId;
    int accountId;
    int amount;
    boolean isDebit;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transaction transaction) {
            return transactionId == transaction.transactionId;
        }
        return false;
    }
}
