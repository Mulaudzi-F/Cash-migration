package transaction.cashin.validator;

import org.junit.jupiter.api.Test;
import transaction.cashin.TransactionService.Validator.CashInValidator;
import transaction.cashin.TransactionService.Dto.TransactionRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CashInValidatorTest {

    private final CashInValidator validator = new CashInValidator();

    private TransactionRequest validRequest() {
        TransactionRequest request = new TransactionRequest();

        request.setTransactionType("CASH_IN");
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrency("MGA");
        request.setSubType("cashin");
        request.setReference("CI-TEST-001");
        request.setInitiatorMsisdn("0341234567");
        request.setReceiverMsisdn("0347654321");

        return request;
    }

    @Test
    void validCashInShouldPass() {
        assertDoesNotThrow(() -> validator.validate(validRequest()));
    }

    @Test
    void validRemoteCashInShouldPass() {
        TransactionRequest request = validRequest();
        request.setSubType("remote_cashin");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void zeroAmountShouldFail() {
        TransactionRequest request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void negativeAmountShouldFail() {
        TransactionRequest request = validRequest();
        request.setAmount(new BigDecimal("-100"));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void invalidTransactionTypeShouldFail() {
        TransactionRequest request = validRequest();
        request.setTransactionType("CASH_OUT");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void missingCurrencyShouldFail() {
        TransactionRequest request = validRequest();
        request.setCurrency(null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void invalidSubtypeShouldFail() {
        TransactionRequest request = validRequest();
        request.setSubType("withdrawal");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void missingReferenceShouldFail() {
        TransactionRequest request = validRequest();
        request.setReference("");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void missingInitiatorShouldFail() {
        TransactionRequest request = validRequest();
        request.setInitiatorMsisdn(null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void missingReceiverShouldFail() {
        TransactionRequest request = validRequest();
        request.setReceiverMsisdn("");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void nullRequestShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null));
    }

    @Test
    void missingAmountShouldFail() {
        TransactionRequest request = validRequest();
        request.setAmount(null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(request));
    }
}
