package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CASH_IN")
public class CashInTransaction extends Transaction {

    public CashInTransaction() {
        super();
    }
}
