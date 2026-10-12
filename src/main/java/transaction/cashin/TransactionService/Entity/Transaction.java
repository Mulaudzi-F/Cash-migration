package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type")
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String currency;

    private String subType;

    private String status;

    private String reference;

    private String quotationReference;

    private String quotationReceipt;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private Party initiator;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Party receiver;

    public Transaction() {
    }

    // Getters

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSubType() {
        return subType;
    }

    public String getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }

    public String getQuotationReference() {
        return quotationReference;
    }

    public String getQuotationReceipt() {
        return quotationReceipt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Party getInitiator() {
        return initiator;
    }

    public Party getReceiver() {
        return receiver;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setQuotationReference(String quotationReference) {
        this.quotationReference = quotationReference;
    }

    public void setQuotationReceipt(String quotationReceipt) {
        this.quotationReceipt = quotationReceipt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setInitiator(Party initiator) {
        this.initiator = initiator;
    }

    public void setReceiver(Party receiver) {
        this.receiver = receiver;
    }
}