
package transaction.cashin.TransactionService.validator;

import org.junit.jupiter.api.Test;
import transaction.cashin.TransactionService.Dto.TransactionRequest;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CashInValidatorTest {
    private final CashInValidator validator = new CashInValidator();
    private TransactionRequest valid() {
        TransactionRequest r = new TransactionRequest();
        r.setTransactionType("CASH_IN"); r.setAmount(new BigDecimal("1000.00"));
        r.setCurrency("MGA"); r.setSubType("cashin"); r.setReference("QA-001");
        r.setInitiatorMsisdn("0341234567"); r.setReceiverMsisdn("0347654321");
        return r;
    }
    @Test void validCashIn() { assertDoesNotThrow(() -> validator.validate(valid())); }
    @Test void validRemoteCashIn() { var r=valid(); r.setSubType("remote_cashin"); assertDoesNotThrow(() -> validator.validate(r)); }
    @Test void nullRequest() { assertThrows(IllegalArgumentException.class, () -> validator.validate(null)); }
    @Test void zeroAmount() { var r=valid(); r.setAmount(BigDecimal.ZERO); assertEquals("Transaction amount must be greater than zero", assertThrows(IllegalArgumentException.class, () -> validator.validate(r)).getMessage()); }
    @Test void negativeAmount() { var r=valid(); r.setAmount(new BigDecimal("-1")); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void nullAmount() { var r=valid(); r.setAmount(null); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void invalidSubtype() { var r=valid(); r.setSubType("cashout"); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void missingReference() { var r=valid(); r.setReference(" "); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void missingInitiator() { var r=valid(); r.setInitiatorMsisdn(null); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void missingReceiver() { var r=valid(); r.setReceiverMsisdn(""); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void missingCurrency() { var r=valid(); r.setCurrency(null); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
    @Test void wrongType() { var r=valid(); r.setTransactionType("CASH_OUT"); assertThrows(IllegalArgumentException.class, () -> validator.validate(r)); }
}
