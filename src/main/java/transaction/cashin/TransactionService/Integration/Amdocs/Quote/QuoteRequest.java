package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

import java.util.List;

public class QuoteRequest {

    private String requestAmount;
    private String requestCurrency;
    private String requestDate;

    private List<QuoteParty> debitParty;
    private List<QuoteParty> creditParty;

    private List<QuoteMetadata> metadata;

    public QuoteRequest() {
    }

    public String getRequestAmount() {
        return requestAmount;
    }

    public void setRequestAmount(String requestAmount) {
        this.requestAmount = requestAmount;
    }

    public String getRequestCurrency() {
        return requestCurrency;
    }

    public void setRequestCurrency(String requestCurrency) {
        this.requestCurrency = requestCurrency;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public List<QuoteParty> getDebitParty() {
        return debitParty;
    }

    public void setDebitParty(List<QuoteParty> debitParty) {
        this.debitParty = debitParty;
    }

    public List<QuoteParty> getCreditParty() {
        return creditParty;
    }

    public void setCreditParty(List<QuoteParty> creditParty) {
        this.creditParty = creditParty;
    }

    public List<QuoteMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<QuoteMetadata> metadata) {
        this.metadata = metadata;
    }
}
