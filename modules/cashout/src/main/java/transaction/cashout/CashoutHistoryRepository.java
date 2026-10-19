package transaction.cashout;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashoutHistoryRepository extends JpaRepository<CashoutHistory, String> {
}
