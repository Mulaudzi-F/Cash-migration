package transaction.cashin.TransactionService.Dto;

import java.math.BigDecimal;

public class CashInOperationRequest {

    private String requestId;
    private String cashPointRef;
    private String customerMsisdn;
    private BigDecimal amount;
    private String cashPointPin;
    private String type;
    private String feeQuoteId;
    private String subtype;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCashPointRef() {
        return cashPointRef;
    }

    public void setCashPointRef(String cashPointRef) {
        this.cashPointRef = cashPointRef;
    }

    public String getCustomerMsisdn() {
        return customerMsisdn;
    }

    public void setCustomerMsisdn(String customerMsisdn) {
        this.customerMsisdn = customerMsisdn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCashPointPin() {
        return cashPointPin;
    }

    public void setCashPointPin(String cashPointPin) {
        this.cashPointPin = cashPointPin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFeeQuoteId() {
        return feeQuoteId;
    }

    public void setFeeQuoteId(String feeQuoteId) {
        this.feeQuoteId = feeQuoteId;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}
