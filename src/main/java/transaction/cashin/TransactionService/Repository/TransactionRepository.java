package transaction.cashin.TransactionService.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import transaction.cashin.TransactionService.Entity.Transaction;


public interface TransactionRepository extends JpaRepository<Transaction, Long>
{
	
}
