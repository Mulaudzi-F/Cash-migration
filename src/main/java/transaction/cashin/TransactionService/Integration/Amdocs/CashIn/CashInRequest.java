package transaction.cashin.TransactionService.Integration.Amdocs.CashIn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CashInRequest {

    private String amount;
    private String currency;
    private String subType;
    private String type;
    private String requestDate;
    @JsonIgnore
    private String username;

    @JsonIgnore
    private String password;

    private List<CashInParty> debitParty;
    private List<CashInParty> creditParty;
    private List<CashInMetadata> metadata;

    public CashInRequest() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<CashInParty> getDebitParty() {
        return debitParty;
    }

    public void setDebitParty(List<CashInParty> debitParty) {
        this.debitParty = debitParty;
    }

    public List<CashInParty> getCreditParty() {
        return creditParty;
    }

    public void setCreditParty(List<CashInParty> creditParty) {
        this.creditParty = creditParty;
    }

    public List<CashInMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<CashInMetadata> metadata) {
        this.metadata = metadata;
    }
}
