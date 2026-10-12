package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CASH_OUT")
public class CashOutTransaction extends Transaction {

    public CashOutTransaction() {
        super();
    }
}
