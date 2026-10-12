package transaction.cashin.TransactionService.Integration.Amdocs.Authentication;

public class AuthenticationRequest {

    private String grantType;
    private String apiKey;
    private String username;
    private String password;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(
            String grantType,
            String apiKey,
            String username,
            String password) {

        this.grantType = grantType;
        this.apiKey = apiKey;
        this.username = username;
        this.password = password;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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
}
