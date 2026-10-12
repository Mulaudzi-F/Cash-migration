package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RECEIVER")
public class ReceiverParty extends Party {

    public ReceiverParty() {
        super();
    }

    public ReceiverParty(String name, String msisdn) {
        super(name, msisdn);
    }
}