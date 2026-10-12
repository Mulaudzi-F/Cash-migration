package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.*;



@Entity
@Table(name = "parties")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "party_type")
public abstract class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String msisdn;

    public Party() {
    }

    public Party(String name, String msisdn) {
        this.name = name;
        this.msisdn = msisdn;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }
}