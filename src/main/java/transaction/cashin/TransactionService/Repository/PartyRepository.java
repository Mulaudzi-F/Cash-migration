package transaction.cashin.TransactionService.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import transaction.cashin.TransactionService.Entity.Party;
import java.util.Optional;



public interface PartyRepository extends JpaRepository<Party, Long> {

    Optional<Party> findByMsisdn(String msisdn);
}
