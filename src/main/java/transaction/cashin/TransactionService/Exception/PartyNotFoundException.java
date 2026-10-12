package transaction.cashin.TransactionService.Exception;

public class PartyNotFoundException extends RuntimeException {

    public PartyNotFoundException(String msisdn) {
        super("Party not found: " + msisdn);
    }
}
