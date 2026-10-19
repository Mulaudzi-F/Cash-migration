package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private final ProducerTemplate producerTemplate;

    @Value("${amdocs.authentication.username}")
    private String username;

    @Value("${amdocs.authentication.password}")
    private String password;

    public QuoteService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public QuoteResponse getQuote(QuoteRequest request) {
        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-quote",
                request,
                "Authorization",
                basicAuthorization(),
                QuoteResponse.class
        );
    }

    public QuoteResponse getQuote(
            QuoteRequest request,
            String accessToken,
            String quoteInitiator) {

        return producerTemplate.requestBodyAndHeaders(
                "direct:amdocs-quote",
                request,
                java.util.Map.of(
                    "Authorization",
                    "Bearer " + accessToken,
                    "quote_initiator",
                    quoteInitiator
                ),
                QuoteResponse.class
        );
    }

    private String basicAuthorization() {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }
}