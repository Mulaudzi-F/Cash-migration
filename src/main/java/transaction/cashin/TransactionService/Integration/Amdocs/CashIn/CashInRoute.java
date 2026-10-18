package transaction.cashin.TransactionService.Integration.Amdocs.CashIn;

import java.time.Instant;
import java.util.UUID;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CashInRoute extends RouteBuilder {

    @Value("${amdocs.client-id}")
    private String clientId;

    @Value("${amdocs.api-key}")
    private String apiKey;

    @Value("${amdocs.source-channel:MobileApp}")
    private String sourceChannel;

    @Value("${amdocs.wait:11}")
    private String wait;

    @Value("${amdocs.suppress-sms:0}")
    private String suppressSms;

    @Override
    public void configure() {

        from("direct:amdocs-cashin")
            .routeId("amdocs-cashin-route")

            // FIX: Generate timestamp directly
            // Prevent Camel from converting CashInRequest to Clock
          .setHeader("Date", () -> Instant.now().toString())

            // Amdocs authentication headers
            .setHeader("X-Client-ID")
                .constant(clientId)

            .setHeader("X-API-Key")
                .constant(apiKey)

            // Unique correlation ID for tracking requests
            .process(exchange ->
                exchange.getMessage().setHeader(
                    "x-correlationid",
                    UUID.randomUUID().toString()
                )
            )

            // Amdocs configuration headers
            .setHeader("source_channel")
                .constant(sourceChannel)

            .setHeader("wait")
                .constant(wait)

            .setHeader("suppress_sms")
                .constant(suppressSms)

            .setHeader("Content-Type")
                .constant("application/json")

            // Convert Java request object into JSON
            .marshal()
                .json()

            // Send Cash-In request to Amdocs
            .to("{{amdocs.cashin.url}}?httpMethod=POST")

            // Convert JSON response into CashInResponse
            .unmarshal()
                .json(CashInResponse.class);
    }
}