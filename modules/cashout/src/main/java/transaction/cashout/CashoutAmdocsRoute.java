package transaction.cashout;

import java.time.Instant;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Camel route for cash-out calls to MVola.
 * Account and group calls use Basic auth on the basic-services prefix.
 * Quote sets CashoutUrl and uses Bearer auth on the services prefix.
 */
@Component
public class CashoutAmdocsRoute extends RouteBuilder {

    @Value("${amdocs.basic-services.url}")
    private String baseUrl;

    @Value("${amdocs.client-id}")
    private String clientId;

    @Value("${amdocs.api-key}")
    private String apiKey;

    @Value("${amdocs.source-channel:MobileApp}")
    private String sourceChannel;

    @Bean("cashoutHeaderFilterStrategy")
    public HeaderFilterStrategy cashoutHeaderFilterStrategy() {
        return new HeaderFilterStrategy() {
            @Override
            public boolean applyFilterToCamelHeaders(String headerName, Object headerValue, Exchange exchange) {
                return false;
            }

            @Override
            public boolean applyFilterToExternalHeaders(String headerName, Object headerValue, Exchange exchange) {
                return false;
            }
        };
    }

    @Override
    public void configure() {
        from("direct:cashout-token")
            .routeId("cashout-token-route")
            .setHeader("Content-Type").constant("application/x-www-form-urlencoded")
            .setHeader("Accept").constant("*/*")
            .to("{{amdocs.authentication.url}}?httpMethod=POST&throwExceptionOnFailure=false");

        from("direct:cashout-amdocs")
            .routeId("cashout-amdocs-route")
            .process(exchange -> exchange.getMessage().setHeader(
                    "Date", Instant.now().toString()))
            .setHeader("X-Client-ID").constant(clientId)
            .setHeader("X-API-Key").constant(apiKey)
            .process(exchange -> exchange.getMessage().setHeader(
                    "x-correlationid", UUID.randomUUID().toString()))
            .setHeader("source_channel").constant(sourceChannel)
            .setHeader("Content-Type").constant("application/json")
            .process(exchange -> {
                String explicit = exchange.getMessage().getHeader("CashoutUrl", String.class);
                String path = exchange.getMessage().getHeader("CashoutPath", String.class);
                String method = exchange.getMessage().getHeader("CashoutHttpMethod", String.class);
                String target = explicit == null || explicit.isBlank()
                        ? stripTrailingSlash(baseUrl) + path
                        : explicit;
                exchange.setProperty(
                        "cashoutTarget",
                        target
                                + "?httpMethod=" + method
                                + "&throwExceptionOnFailure=false"
                                + "&headerFilterStrategy=#cashoutHeaderFilterStrategy");
            })
            .toD("${exchangeProperty.cashoutTarget}");
    }

    private static String stripTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
