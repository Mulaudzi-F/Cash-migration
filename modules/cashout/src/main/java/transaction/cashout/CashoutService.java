package transaction.cashout;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CashoutService {

    private final CashoutHistoryRepository historyRepository;
    private final CellCityLookup cellCityLookup;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;
    private final String grantType;
    private final String apiKey;
    private final String username;
    private final String password;
    private final String quoteUrl;

    public CashoutService(
            CashoutHistoryRepository historyRepository,
            CellCityLookup cellCityLookup,
            ProducerTemplate producerTemplate,
            ObjectMapper objectMapper,
            @Value("${amdocs.authentication.grant-type}") String grantType,
            @Value("${amdocs.authentication.api-key}") String apiKey,
            @Value("${amdocs.authentication.username}") String username,
            @Value("${amdocs.authentication.password}") String password,
            @Value("${amdocs.quote.url}") String quoteUrl) {
        this.historyRepository = historyRepository;
        this.cellCityLookup = cellCityLookup;
        this.producerTemplate = producerTemplate;
        this.objectMapper = objectMapper;
        this.grantType = grantType;
        this.apiKey = apiKey;
        this.username = username;
        this.password = password;
        this.quoteUrl = quoteUrl;
    }

    public CashoutFeeResponse getFee(CashoutFeeRequest request) {
        if (request == null
                || isBlank(request.getCashPointMsisdn())
                || isBlank(request.getCustomerMsisdn())
                || request.getAmount() == null
                || isBlank(request.getCashPointCGI())
                || isBlank(request.getSubtype())) {
            throw new IllegalArgumentException("Invalid msisdn or amount format");
        }
        if (!"cashout".equalsIgnoreCase(request.getSubtype())
                && !"remote_cashout".equalsIgnoreCase(request.getSubtype())) {
            throw new IllegalArgumentException("Invalid msisdn or amount format");
        }
        if (isBlank(request.getCustomerCGI()) || "0".equals(request.getCustomerCGI().trim())) {
            return zeroFee();
        }
        String cashPointCity = cellCityLookup.cityFor(request.getCashPointCGI());
        if (isBlank(cashPointCity)) {
            return zeroFee();
        }
        String customerCity = cellCityLookup.cityFor(request.getCustomerCGI());
        if (isBlank(customerCity) || cashPointCity.equalsIgnoreCase(customerCity)) {
            return zeroFee();
        }
        String quoteJson = "{"
                + "\"requestAmount\":\"" + request.getAmount().toPlainString() + "\","
                + "\"requestCurrency\":\"MGA\","
                + "\"requestDate\":\"" + Instant.now() + "\","
                + "\"debitParty\":[{\"key\":\"accountid\",\"value\":\""
                + request.getCashPointMsisdn() + "\"}],"
                + "\"creditParty\":[{\"key\":\"accountid\",\"value\":\""
                + request.getCustomerMsisdn() + "\"}],"
                + "\"metadata\":["
                + "{\"key\":\"cashPointCGI\",\"value\":\"" + request.getCashPointCGI() + "\"},"
                + "{\"key\":\"customerCGI\",\"value\":\"" + request.getCustomerCGI() + "\"},"
                + "{\"key\":\"subType\",\"value\":\"" + request.getSubtype() + "\"}"
                + "]}";
        AmdocsCall quote = quote(quoteJson, request.getCashPointMsisdn());
        if (quote.statusCode < 200 || quote.statusCode >= 300) {
            CashoutFeeResponse failed = new CashoutFeeResponse();
            failed.setStatusCode(500);
            failed.setStatusReason("Technical Error");
            failed.setFees(BigDecimal.ZERO);
            return failed;
        }
        CashoutFeeResponse response = new CashoutFeeResponse();
        response.setStatusCode(200);
        response.setStatusReason("Fee fetched from AMDOCS");
        response.setFees(readFee(quote.body));
        return response;
    }

    public CashoutResponse cashout(CashoutRequest request) {
        if (request == null
                || isBlank(request.getRequestId())
                || isBlank(request.getInitiatorMsisdn())
                || isBlank(request.getAgentRef())
                || isBlank(request.getCustomerPIN())
                || request.getAmount() == null) {
            throw new IllegalArgumentException("Bad Inputs");
        }
        Optional<CashoutHistory> existing =
                historyRepository.findById(request.getRequestId());
        if (existing.isPresent()) {
            return fromHistory(existing.get());
        }
        if (request.getAmount().remainder(new BigDecimal("100"))
                .compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Bad Inputs");
        }

        CashoutHistory history = new CashoutHistory();
        history.setId(request.getRequestId());
        history.setMsisdnInitiator(request.getInitiatorMsisdn());
        history.setMsisdnCreditor(request.getAgentRef());
        history.setDateTransaction(LocalDateTime.now());
        history.setAmount(request.getAmount());
        history.setChannel(request.getChannelId());
        history.setStatus("INITIATED");
        history.setFees(BigDecimal.ZERO);
        historyRepository.save(history);

        String user = request.getInitiatorMsisdn();
        String pin = request.getCustomerPIN();

        AmdocsCall initiator = call("GET", accountPath(user), null, user, pin);
        if (initiator.statusCode != 200) {
            return finish(history, "FAILED", amdocsReason(initiator), null);
        }
        AmdocsCall agent = call("GET", accountPath(request.getAgentRef()), null, user, pin);
        if (agent.statusCode != 200) {
            return finish(history, "FAILED", amdocsReason(agent), null);
        }

        AmdocsCall groups = call("GET", accountPath(user) + "/groups", null, user, pin);
        if (groups.statusCode != 200) {
            return finish(history, "REJECTED", amdocsReason(groups), null);
        }

        return finish(
                history,
                "REJECTED",
                "Mini statement URL is not in the cash-out specification, so the weekly counter cannot be checked",
                null);
    }

    private CashoutResponse finish(
            CashoutHistory history,
            String status,
            String reason,
            String transId) {
        history.setStatus(status);
        history.setStatusReason(reason);
        history.setTransId(transId);
        historyRepository.save(history);
        CashoutResponse response = new CashoutResponse();
        response.setStatus(status);
        response.setStatusReason(reason);
        response.setTransactionReference(transId);
        return response;
    }

    private CashoutResponse fromHistory(CashoutHistory history) {
        CashoutResponse response = new CashoutResponse();
        response.setStatus(history.getStatus());
        response.setStatusReason(history.getStatusReason());
        response.setTransactionReference(history.getTransId());
        return response;
    }

    private CashoutFeeResponse zeroFee() {
        CashoutFeeResponse response = new CashoutFeeResponse();
        response.setStatusCode(200);
        response.setStatusReason("Fee = 0");
        response.setFees(BigDecimal.ZERO);
        return response;
    }

    private BigDecimal readFee(String body) {
        try {
            JsonNode fees = objectMapper.readTree(body).path("fees");
            BigDecimal total = BigDecimal.ZERO;
            if (fees.isArray()) {
                for (JsonNode fee : fees) {
                    for (JsonNode amount : fee.path("amounts")) {
                        String value = amount.path("amount").asText("");
                        if (!value.isBlank()) {
                            total = total.add(new BigDecimal(value));
                        }
                    }
                }
            }
            return total;
        } catch (Exception exception) {
            return BigDecimal.ZERO;
        }
    }

    private AmdocsCall quote(String json, String initiator) {
        String token = accessToken();
        if (token == null) {
            return new AmdocsCall(500, "");
        }
        Exchange exchange = producerTemplate.request("direct:cashout-amdocs", callback -> {
            callback.getMessage().setHeader("Authorization", "Bearer " + token);
            callback.getMessage().setHeader("quote_initiator", initiator);
            callback.getMessage().setHeader("CashoutUrl", quoteUrl);
            callback.getMessage().setHeader("CashoutHttpMethod", "POST");
            callback.getMessage().setBody(json);
        });
        return toCall(exchange);
    }

    private AmdocsCall toCall(Exchange exchange) {
        Integer statusCode = exchange.getMessage().getHeader("CamelHttpResponseCode", Integer.class);
        String body = exchange.getMessage().getBody(String.class);
        return new AmdocsCall(statusCode == null ? 500 : statusCode, body == null ? "" : body);
    }

    private String accessToken() {
        String form = "grant_type=" + encode(grantType)
                + "&api_key=" + encode(apiKey)
                + "&password=" + encode(password)
                + "&username=" + encode(username);
        Exchange exchange = producerTemplate.request("direct:cashout-token", callback -> {
            callback.getMessage().setBody(form);
        });
        String body = exchange.getMessage().getBody(String.class);
        try {
            String token = objectMapper.readTree(body == null ? "" : body).path("access_token").asText("");
            return token.isBlank() ? null : token;
        } catch (Exception exception) {
            return null;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private AmdocsCall call(
            String method,
            String path,
            String json,
            String username,
            String password) {
        Exchange exchange = producerTemplate.request("direct:cashout-amdocs", callback -> {
            callback.getMessage().setHeader("Authorization", basic(username, password));
            callback.getMessage().setHeader("CashoutPath", path);
            callback.getMessage().setHeader("CashoutHttpMethod", method);
            if (json != null) {
                callback.getMessage().setBody(json);
            }
        });
        return toCall(exchange);
    }

    private String amdocsReason(AmdocsCall response) {
        try {
            JsonNode node = objectMapper.readTree(response.body);
            String description = node.path("errorDescription").asText("");
            if (!description.isBlank()) {
                return description;
            }
        } catch (Exception ignored) {
            // Body was not the Amdocs error shape.
        }
        return "Technical Error";
    }

    private static String basic(String username, String password) {
        String credentials = username + ":" + (password == null ? "" : password);
        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static String accountPath(String msisdn) {
        return "/v2/MM/accounts/accountid%40" + msisdn;
    }

    private static final class AmdocsCall {
        private final int statusCode;
        private final String body;

        private AmdocsCall(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
