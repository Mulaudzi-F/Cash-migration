package transaction.cashin.TransactionService.Dto;

import java.math.BigDecimal;

public class FeeRequest {

    private String cashPointMsisdn;
    private String customerMsisdn;
    private BigDecimal amount;
    private String cashPointCGI;
    private String customerCGI;
    private String subtype;

    public String getCashPointMsisdn() {
        return cashPointMsisdn;
    }

    public void setCashPointMsisdn(String cashPointMsisdn) {
        this.cashPointMsisdn = cashPointMsisdn;
    }

    public String getCustomerMsisdn() {
        return customerMsisdn;
    }

    public void setCustomerMsisdn(String customerMsisdn) {
        this.customerMsisdn = customerMsisdn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCashPointCGI() {
        return cashPointCGI;
    }

    public void setCashPointCGI(String cashPointCGI) {
        this.cashPointCGI = cashPointCGI;
    }

    public String getCustomerCGI() {
        return customerCGI;
    }

    public void setCustomerCGI(String customerCGI) {
        this.customerCGI = customerCGI;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}
