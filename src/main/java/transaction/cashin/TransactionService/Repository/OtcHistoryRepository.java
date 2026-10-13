package transaction.cashin.TransactionService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import transaction.cashin.TransactionService.Entity.OtcHistory;

public interface OtcHistoryRepository extends JpaRepository<OtcHistory, Long> {
}
