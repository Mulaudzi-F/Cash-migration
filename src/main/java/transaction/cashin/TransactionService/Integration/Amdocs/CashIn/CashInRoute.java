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

            .process(exchange ->
                exchange.getMessage().setHeader(
                    "Date",
                    Instant.now().toString()
                )
            )

            .setHeader("X-Client-ID")
                .constant(clientId)

            .setHeader("X-API-Key")
                .constant(apiKey)

            .process(exchange ->
                exchange.getMessage().setHeader(
                    "x-correlationid",
                    UUID.randomUUID().toString()
                )
            )

            .setHeader("source_channel")
                .constant(sourceChannel)

            .setHeader("wait")
                .constant(wait)

            .setHeader("suppress_sms")
                .constant(suppressSms)

            .setHeader("Content-Type")
                .constant("application/json")

            .marshal()
                .json()

            .to("{{amdocs.cashin.url}}?httpMethod=POST"
                    + "&headerFilterStrategy=#dateHeaderFilterStrategy")

            .unmarshal()
                .json(CashInResponse.class);
    }
}
