package transaction.cashin.TransactionService.Integration.Amdocs.CashIn;

public class CashInParty {

    private String key;
    private String value;

    public CashInParty() {
    }

    public CashInParty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}