package transaction.cashin.TransactionService.Integration.Amdocs.CashIn;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CashInService {

    private final ProducerTemplate producerTemplate;

    @Value("${amdocs.authentication.username}")
    private String username;

    @Value("${amdocs.authentication.password}")
    private String password;

    public CashInService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public CashInResponse executeCashIn(CashInRequest request) {
        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-cashin",
                request,
                "Authorization",
                basicAuthorization(),
                CashInResponse.class
        );
    }

    public CashInResponse executeCashIn(
            CashInRequest request,
            String accessToken) {

        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-cashin",
                request,
                "Authorization",
                "Bearer " + accessToken,
                CashInResponse.class
        );
    }

    private String basicAuthorization() {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }
}
