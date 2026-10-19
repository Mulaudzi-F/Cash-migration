package transaction.cashout;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mb_cashout_transaction")
public class CashoutHistory {

    @Id
    private String id;
    private String msisdnInitiator;
    private String msisdnCreditor;
    private LocalDateTime dateTransaction;
    private BigDecimal amount;
    private String channel;
    private String status;
    private String statusReason;
    private BigDecimal fees;
    private String transId;
    private String cashoutType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsisdnInitiator() {
        return msisdnInitiator;
    }

    public void setMsisdnInitiator(String msisdnInitiator) {
        this.msisdnInitiator = msisdnInitiator;
    }

    public String getMsisdnCreditor() {
        return msisdnCreditor;
    }

    public void setMsisdnCreditor(String msisdnCreditor) {
        this.msisdnCreditor = msisdnCreditor;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(LocalDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getCashoutType() {
        return cashoutType;
    }

    public void setCashoutType(String cashoutType) {
        this.cashoutType = cashoutType;
    }
}
