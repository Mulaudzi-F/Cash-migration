package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

import java.util.List;

public class QuoteFee {

    private List<QuoteAmount> amounts;
    private String transactionReference;
    private String creditor;
    private String debtor;

    public QuoteFee() {
    }

    public List<QuoteAmount> getAmounts() {
        return amounts;
    }

    public void setAmounts(List<QuoteAmount> amounts) {
        this.amounts = amounts;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getCreditor() {
        return creditor;
    }

    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    public String getDebtor() {
        return debtor;
    }

    public void setDebtor(String debtor) {
        this.debtor = debtor;
    }
}
