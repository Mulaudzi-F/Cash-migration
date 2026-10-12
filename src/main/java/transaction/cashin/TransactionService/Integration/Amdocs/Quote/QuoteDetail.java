package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

public class QuoteDetail {

    private String receivingAmount;
    private String receivingCurrency;
    private String sendingAmount;
    private String sendingCurrency;

    public QuoteDetail() {
    }

    public String getReceivingAmount() {
        return receivingAmount;
    }

    public void setReceivingAmount(String receivingAmount) {
        this.receivingAmount = receivingAmount;
    }

    public String getReceivingCurrency() {
        return receivingCurrency;
    }

    public void setReceivingCurrency(String receivingCurrency) {
        this.receivingCurrency = receivingCurrency;
    }

    public String getSendingAmount() {
        return sendingAmount;
    }

    public void setSendingAmount(String sendingAmount) {
        this.sendingAmount = sendingAmount;
    }

    public String getSendingCurrency() {
        return sendingCurrency;
    }

    public void setSendingCurrency(String sendingCurrency) {
        this.sendingCurrency = sendingCurrency;
    }
}