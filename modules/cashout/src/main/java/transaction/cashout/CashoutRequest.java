package transaction.cashout;

import java.math.BigDecimal;

public class CashoutRequest {

    private String correlationId;
    private String requestId;
    private String channelId;
    private String initiatorMsisdn;
    private BigDecimal amount;
    private String agentRef;
    private String customerPIN;
    private String feeQuoteId;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getInitiatorMsisdn() {
        return initiatorMsisdn;
    }

    public void setInitiatorMsisdn(String initiatorMsisdn) {
        this.initiatorMsisdn = initiatorMsisdn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAgentRef() {
        return agentRef;
    }

    public void setAgentRef(String agentRef) {
        this.agentRef = agentRef;
    }

    public String getCustomerPIN() {
        return customerPIN;
    }

    public void setCustomerPIN(String customerPIN) {
        this.customerPIN = customerPIN;
    }

    public String getFeeQuoteId() {
        return feeQuoteId;
    }

    public void setFeeQuoteId(String feeQuoteId) {
        this.feeQuoteId = feeQuoteId;
    }
}
