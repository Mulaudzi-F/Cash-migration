package transaction.cashin.TransactionService.Validator;

import org.springframework.stereotype.Component;

import transaction.cashin.TransactionService.Dto.TransactionRequest;

@Component
public class CashInValidator {

    public void validate(TransactionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request cannot be null");
        }

        validateTransactionType(request);
        validateAmount(request);
        validateCurrency(request);
        validateSubType(request);
        validateReference(request);
        validateInitiator(request);
        validateReceiver(request);
    }

    private void validateTransactionType(TransactionRequest request) {

        if (request.getTransactionType() == null ||
                request.getTransactionType().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction type is required");
        }

        if (!"CASH_IN".equalsIgnoreCase(
                request.getTransactionType())) {

            throw new IllegalArgumentException(
                    "Only CASH_IN transactions are supported");
        }
    }

    private void validateAmount(TransactionRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero");
        }
    }

    private void validateCurrency(TransactionRequest request) {

        if (request.getCurrency() == null ||
                request.getCurrency().isBlank()) {

            throw new IllegalArgumentException(
                    "Currency is required");
        }
    }

    private void validateSubType(TransactionRequest request) {

        if (request.getSubType() == null ||
                request.getSubType().isBlank()) {

            throw new IllegalArgumentException(
                    "Cash-In subtype is required");
        }

        if (!"cashin".equalsIgnoreCase(request.getSubType()) &&
                !"remote_cashin".equalsIgnoreCase(request.getSubType())) {

            throw new IllegalArgumentException(
                    "Invalid Cash-In subtype. " +
                    "Expected cashin or remote_cashin");
        }
    }

    private void validateReference(TransactionRequest request) {

        if (request.getReference() == null ||
                request.getReference().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction reference is required");
        }
    }

    private void validateInitiator(TransactionRequest request) {

        if (request.getInitiatorMsisdn() == null ||
                request.getInitiatorMsisdn().isBlank()) {

            throw new IllegalArgumentException(
                    "Initiator MSISDN is required");
        }
    }

    private void validateReceiver(TransactionRequest request) {

        if (request.getReceiverMsisdn() == null ||
                request.getReceiverMsisdn().isBlank()) {

            throw new IllegalArgumentException(
                    "Receiver MSISDN is required");
        }
    }
}