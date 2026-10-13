package transaction.cashin.TransactionService.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mb_otc_history")
public class OtcHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operationId;
    private String initiateur;
    private String beneficiaire;
    private BigDecimal montant;
    private String transId;
    private String operationType;
    private LocalDateTime operationDate;
    private Integer counterCashin;

    public Long getId() {
        return id;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getInitiateur() {
        return initiateur;
    }

    public void setInitiateur(String initiateur) {
        this.initiateur = initiateur;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public LocalDateTime getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDateTime operationDate) {
        this.operationDate = operationDate;
    }

    public Integer getCounterCashin() {
        return counterCashin;
    }

    public void setCounterCashin(Integer counterCashin) {
        this.counterCashin = counterCashin;
    }
}
