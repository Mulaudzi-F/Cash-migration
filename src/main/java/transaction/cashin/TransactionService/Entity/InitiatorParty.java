package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INITIATOR")
public class InitiatorParty extends Party {

    public InitiatorParty() {
        super();
    }

    public InitiatorParty(String name, String msisdn) {
        super(name, msisdn);
    }
}