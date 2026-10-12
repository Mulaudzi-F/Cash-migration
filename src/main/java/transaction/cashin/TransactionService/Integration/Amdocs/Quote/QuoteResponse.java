package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

import java.util.List;

public class QuoteResponse {

    private String quotationReference;

    private List<QuoteParty> creditParty;
    private List<QuoteParty> debitParty;

    private String requestAmount;

    private List<QuoteDetail> quotes;

    private List<QuoteMetadata> metadata;

    private List<QuoteFee> fees;
    private List<QuoteFee> bonuses;

    private String quotationReceipt;

    public QuoteResponse() {
    }

    public String getQuotationReference() {
        return quotationReference;
    }

    public void setQuotationReference(String quotationReference) {
        this.quotationReference = quotationReference;
    }

    public List<QuoteParty> getCreditParty() {
        return creditParty;
    }

    public void setCreditParty(List<QuoteParty> creditParty) {
        this.creditParty = creditParty;
    }

    public List<QuoteParty> getDebitParty() {
        return debitParty;
    }

    public void setDebitParty(List<QuoteParty> debitParty) {
        this.debitParty = debitParty;
    }

    public String getRequestAmount() {
        return requestAmount;
    }

    public void setRequestAmount(String requestAmount) {
        this.requestAmount = requestAmount;
    }

    public List<QuoteDetail> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<QuoteDetail> quotes) {
        this.quotes = quotes;
    }

    public List<QuoteMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<QuoteMetadata> metadata) {
        this.metadata = metadata;
    }

    public List<QuoteFee> getFees() {
        return fees;
    }

    public void setFees(List<QuoteFee> fees) {
        this.fees = fees;
    }

    public List<QuoteFee> getBonuses() {
        return bonuses;
    }

    public void setBonuses(List<QuoteFee> bonuses) {
        this.bonuses = bonuses;
    }

    public String getQuotationReceipt() {
        return quotationReceipt;
    }

    public void setQuotationReceipt(String quotationReceipt) {
        this.quotationReceipt = quotationReceipt;
    }
}
