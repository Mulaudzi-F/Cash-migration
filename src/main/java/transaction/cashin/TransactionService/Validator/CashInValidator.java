package transaction.cashin.TransactionService.Validator;

import org.springframework.stereotype.Component;

import transaction.cashin.TransactionService.Dto.CashInOperationRequest;
import transaction.cashin.TransactionService.Dto.TransactionRequest;

@Component
public class CashInValidator {

    public void validate(CashInOperationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request cannot be null");
        }

        validateTransactionType(request);
        validateAmount(request);
       // validateCurrency(request);
        validateSubType(request);
        validateReference(request);
      //  validateInitiator(request);
        validateReceiver(request);
    }

    
 
    
    
    private void validateTransactionType(CashInOperationRequest request) {

        if (request.getType() == null ||
                request.getType().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction type is required");
        }

        if (!"CASH_IN".equalsIgnoreCase(
                request.getType())) {

            throw new IllegalArgumentException(
                    "Only CASH_IN transactions are supported");
        }
    }

    private void validateAmount(CashInOperationRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero");
        }
    }

    /*
    private void validateCurrency(CashInOperationRequest request) {

        if (request.getCurrency() == null ||
                request.getCurrency().isBlank()) {

            throw new IllegalArgumentException(
                    "Currency is required");
        }
    }*/

    private void validateSubType(CashInOperationRequest request) {

        if (request.getSubtype() == null ||
                request.getSubtype().isBlank()) {

            throw new IllegalArgumentException(
                    "Cash-In subtype is required");
        }

        if (!"cashin".equalsIgnoreCase(request.getSubtype()) &&
                !"remote_cashin".equalsIgnoreCase(request.getSubtype())) {

            throw new IllegalArgumentException(
                    "Invalid Cash-In subtype. " +
                    "Expected cashin or remote_cashin");
        }
    }

    private void validateReference(CashInOperationRequest request) {

        if (request.getCashPointRef() == null ||
                request.getCashPointRef().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction reference is required");
        }
    }

    /*
    private void validateInitiator(CashInOperationRequest request) {

        if (request.getInitiatorMsisdn() == null ||
                request.getInitiatorMsisdn().isBlank()) {

            throw new IllegalArgumentException(
                    "Initiator MSISDN is required");
        }
    }*/

    private void validateReceiver(CashInOperationRequest request) {

        if (request.getCustomerMsisdn() == null ||
                request.getCustomerMsisdn().isBlank()) {

            throw new IllegalArgumentException(
                    "Receiver MSISDN is required");
        }
    }
}