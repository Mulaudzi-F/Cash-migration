package transaction.cashin.TransactionService.Validator;

import org.springframework.stereotype.Component;

import org.springframework.stereotype.Component;

import transaction.cashin.TransactionService.Dto.CashInOperationRequest;
import transaction.cashin.TransactionService.Dto.TransactionRequest;

@Component
public class TransactionValidator {

    public void validate(CashInOperationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request cannot be null");
        }
    }
}
