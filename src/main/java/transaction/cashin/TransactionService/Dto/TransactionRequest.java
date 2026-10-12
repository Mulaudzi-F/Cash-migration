package transaction.cashin.TransactionService.Dto;



import java.math.BigDecimal;

public class TransactionRequest {

    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String subType;
    private String reference;
    private String initiatorMsisdn;
    private String receiverMsisdn;

    public TransactionRequest() {
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getInitiatorMsisdn() {
        return initiatorMsisdn;
    }

    public void setInitiatorMsisdn(String initiatorMsisdn) {
        this.initiatorMsisdn = initiatorMsisdn;
    }

    public String getReceiverMsisdn() {
        return receiverMsisdn;
    }

    public void setReceiverMsisdn(String receiverMsisdn) {
        this.receiverMsisdn = receiverMsisdn;
    }
}
