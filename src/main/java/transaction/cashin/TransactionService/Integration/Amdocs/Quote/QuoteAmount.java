package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

public class QuoteAmount {

    private String amount;
    private String walletKey;

    public QuoteAmount() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getWalletKey() {
        return walletKey;
    }

    public void setWalletKey(String walletKey) {
        this.walletKey = walletKey;
    }
}
