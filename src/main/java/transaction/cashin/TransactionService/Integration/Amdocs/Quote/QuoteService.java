package transaction.cashin.TransactionService.Integration.Amdocs.Quote;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private final ProducerTemplate producerTemplate;

    public QuoteService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
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
}