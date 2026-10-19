package transaction.cashin.TransactionService.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import transaction.cashin.TransactionService.Entity.OtcHistorySuccess;

public interface OtcHistorySuccessRepository
        extends JpaRepository<OtcHistorySuccess, Long> {

    List<OtcHistorySuccess> findByBeneficiaireOrderByOperationDateDesc(
            String beneficiaire);
}
