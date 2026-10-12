package transaction.cashin.TransactionService.Integration.Amdocs.Authentication;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("direct:amdocs-authentication")
            .routeId("amdocs-authentication-route")

            .setHeader("Content-Type")
                .constant(
                    "application/x-www-form-urlencoded")

            .process(exchange -> {

                AuthenticationRequest request =
                        exchange.getMessage()
                                .getBody(
                                    AuthenticationRequest.class);

                String formBody =
                        "grant_type="
                        + encode(request.getGrantType())
                        + "&api_key="
                        + encode(request.getApiKey())
                        + "&password="
                        + encode(request.getPassword())
                        + "&username="
                        + encode(request.getUsername());

                exchange.getMessage()
                        .setBody(formBody);
            })

            .to("{{amdocs.authentication.url}}?httpMethod=POST")

            .unmarshal()
                .json(AuthenticationResponse.class);
    }

    private String encode(String value) {

        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8);
    }
}