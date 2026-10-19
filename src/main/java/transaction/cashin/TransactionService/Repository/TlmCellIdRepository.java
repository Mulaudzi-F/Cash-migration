package transaction.cashin.TransactionService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import transaction.cashin.TransactionService.Entity.TlmCellId;

public interface TlmCellIdRepository extends JpaRepository<TlmCellId, String> {
}
