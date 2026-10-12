package transaction.cashin.TransactionService.Integration.Amdocs.Group;

import org.apache.camel.Exchange;
import java.time.Instant;
import java.util.UUID;

import org.apache.camel.spi.HeaderFilterStrategy;
import org.springframework.context.annotation.Bean;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpHeaderFilterStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import org.apache.camel.spi.HeaderFilterStrategy;
import org.springframework.context.annotation.Bean;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.springframework.context.annotation.Bean;

@Component
public class GroupRoute extends RouteBuilder {

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
    
    
    
    
    @Bean("dateHeaderFilterStrategy")
    public HeaderFilterStrategy dateHeaderFilterStrategy() {
        return new HeaderFilterStrategy() {

            @Override
            public boolean applyFilterToCamelHeaders(
                    String headerName,
                    Object headerValue,
                    org.apache.camel.Exchange exchange) {

                return false;
            }

            @Override
            public boolean applyFilterToExternalHeaders(
                    String headerName,
                    Object headerValue,
                    org.apache.camel.Exchange exchange) {

                return false;
            }
        };
    }
    
    
    

    @Override
    public void configure() {

        from("direct:amdocs-get-group")
            .routeId("amdocs-get-group-route")

            // =====================================================
            // Validate and build account group path
            // =====================================================

            .process(exchange -> {

                GroupRequest request =
                        exchange.getMessage()
                                .getBody(GroupRequest.class);

                if (request == null ||
                        request.getReference() == null ||
                        request.getReference().isBlank()) {

                    throw new IllegalArgumentException(
                            "Group account reference is required");
                }

                String groupPath =
                        "accountid@"
                        + request.getReference()
                        + "/groups";

                exchange.setProperty(
                        "groupPath",
                        groupPath);
            })

            // =====================================================
            // Common Amdocs headers
            // =====================================================

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

            // =====================================================
            // GET Group API
            // =====================================================
                
            .log("Date header before HTTP call: ${header.Date}")
            .log("DFS Group request body: ${body}")


            .setHeader("CamelHttpMethod")
                .constant("GET")

            .toD(
                "{{amdocs.group.url}}/"
                + "${exchangeProperty.groupPath}"
                + "?httpMethod=GET"
                + "&headerFilterStrategy=#dateHeaderFilterStrategy"
            )
            
            
            .log("DFS Group HTTP status: ${header.CamelHttpResponseCode}")
            .log("DFS Group response body: ${body}")

            // =====================================================
            // Deserialize response
            // =====================================================

            .unmarshal()
                .json(GroupResponse.class);
    }
}