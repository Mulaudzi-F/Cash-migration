package transaction.cashin.TransactionService.Integration.Amdocs.Group;

public class GroupRequest {

    private String reference;

    public GroupRequest() {
    }

    public GroupRequest(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}