package transaction.cashin.TransactionService.Dto;



import java.math.BigDecimal;



public class TransactionResponse {

    private Long transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String reference;

    private String quotationReference;
    private String quotationReceipt;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long transactionId,
            String transactionType,
            BigDecimal amount,
            String currency,
            String status,
            String reference,
            String quotationReference,
            String quotationReceipt) {

        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reference = reference;
        this.quotationReference = quotationReference;
        this.quotationReceipt = quotationReceipt;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }

    public String getQuotationReference() {
        return quotationReference;
    }

    public String getQuotationReceipt() {
        return quotationReceipt;
    }
}