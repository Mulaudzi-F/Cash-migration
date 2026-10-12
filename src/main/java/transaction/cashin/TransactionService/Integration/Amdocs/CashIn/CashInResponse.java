package transaction.cashin.TransactionService.Integration.Amdocs.CashIn;

import java.util.List;
import java.util.Map;

public class CashInResponse {

    private String transactionReference;
    private String requestingOrganisationTransactionReference;

    private List<CashInParty> creditParty;
    private List<CashInParty> debitParty;

    private String type;
    private String subType;
    private String transactionStatus;
    private String amount;
    private String currency;
    private String transactionReceipt;

    /*
     * The API example represents these as complex date/time objects.
     * We keep them flexible until the exact response schema is confirmed.
     */
    private Map<String, Object> creationDate;
    private Map<String, Object> requestDate;

    private List<CashInMetadata> metadata;

    public CashInResponse() {
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getRequestingOrganisationTransactionReference() {
        return requestingOrganisationTransactionReference;
    }

    public void setRequestingOrganisationTransactionReference(
            String requestingOrganisationTransactionReference) {
        this.requestingOrganisationTransactionReference =
                requestingOrganisationTransactionReference;
    }

    public List<CashInParty> getCreditParty() {
        return creditParty;
    }

    public void setCreditParty(List<CashInParty> creditParty) {
        this.creditParty = creditParty;
    }

    public List<CashInParty> getDebitParty() {
        return debitParty;
    }

    public void setDebitParty(List<CashInParty> debitParty) {
        this.debitParty = debitParty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
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

    public String getTransactionReceipt() {
        return transactionReceipt;
    }

    public void setTransactionReceipt(String transactionReceipt) {
        this.transactionReceipt = transactionReceipt;
    }

    public Map<String, Object> getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Map<String, Object> creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, Object> getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Map<String, Object> requestDate) {
        this.requestDate = requestDate;
    }

    public List<CashInMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<CashInMetadata> metadata) {
        this.metadata = metadata;
    }
}