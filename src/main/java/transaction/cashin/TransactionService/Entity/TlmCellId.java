package transaction.cashin.TransactionService.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tlm_cellid")
public class TlmCellId {

    @Id
    private String cgi;

    private String ville;

    public String getCgi() {
        return cgi;
    }

    public void setCgi(String cgi) {
        this.cgi = cgi;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }
}
