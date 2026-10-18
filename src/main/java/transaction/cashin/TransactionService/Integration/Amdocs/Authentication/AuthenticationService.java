package transaction.cashin.TransactionService.Integration.Amdocs.Authentication;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final ProducerTemplate producerTemplate;

    @Value("${amdocs.authentication.grant-type}")
    private String grantType;

    @Value("${amdocs.authentication.api-key}")
    private String apiKey;

    @Value("${amdocs.authentication.username}")
    private String username;

    @Value("${amdocs.authentication.password}")
    private String password;

    public AuthenticationService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public AuthenticationResponse authenticate() {

        // Step 1: Build the authentication request
        AuthenticationRequest request = new AuthenticationRequest(
                grantType,
                apiKey,
                username,
                password
        );

        // Step 2: Send the request through Apache Camel
        AuthenticationResponse response = producerTemplate.requestBody(
                "direct:amdocs-authentication",
                request,
                AuthenticationResponse.class
        );

        // Step 3: Validate the authentication response
        if (response == null
                || response.getAccessToken() == null
                || response.getAccessToken().isBlank()) {

            throw new IllegalStateException(
                    "Amdocs authentication failed: no access token returned"
            );
        }

        // Step 4: Return the authentication response
        return response;
    }
}