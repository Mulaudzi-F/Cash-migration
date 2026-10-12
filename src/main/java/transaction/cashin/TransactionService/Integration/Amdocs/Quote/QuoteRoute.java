package transaction.cashin.TransactionService.Integration.Amdocs.Quote;


import java.time.Instant;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.camel.http.base.HttpHeaderFilterStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



import org.springframework.context.annotation.Bean;
import org.apache.camel.spi.HeaderFilterStrategy;



@Component
public class QuoteRoute extends RouteBuilder {

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
    
    
    
    
    @Bean("dateFilterStrategy")
    public HeaderFilterStrategy dateFilterStrategy() {
        return new HeaderFilterStrategy() {

            @Override
            public boolean applyFilterToCamelHeaders(
                    String headerName,
                    Object headerValue,
                    Exchange exchange) {

                return false;
            }

            @Override
            public boolean applyFilterToExternalHeaders(
                    String headerName,
                    Object headerValue,
                    Exchange exchange) {

                return false;
            }
        };
    }
    
    
    
    

    @Override
    public void configure() {

        from("direct:amdocs-quote")
            .routeId("amdocs-quote-route")

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
                
                
            .log("Date header before HTTP call: ${header.Date}")
            .log("DFS Quote request body: ${body}")

           // .to("{{amdocs.quote.url}}?httpMethod=POST")
            .to("{{amdocs.quote.url}}"
                    + "?httpMethod=POST"
                    + "&throwExceptionOnFailure=false"
                    + "&headerFilterStrategy=#dateFilterStrategy")
            
            
            
            
            
            .log("DFS Quote HTTP status: ${header.CamelHttpResponseCode}")
            .log("DFS Quote response body: ${body}")

            .unmarshal()
                .json(QuoteResponse.class);
    }
}
