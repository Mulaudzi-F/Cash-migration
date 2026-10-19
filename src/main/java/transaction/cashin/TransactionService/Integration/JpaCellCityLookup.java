package transaction.cashin.TransactionService.Integration;

import org.springframework.stereotype.Component;

import transaction.cashin.TransactionService.Entity.TlmCellId;
import transaction.cashin.TransactionService.Repository.TlmCellIdRepository;
import transaction.cashout.CellCityLookup;

@Component
public class JpaCellCityLookup implements CellCityLookup {

    private final TlmCellIdRepository tlmCellIdRepository;

    public JpaCellCityLookup(TlmCellIdRepository tlmCellIdRepository) {
        this.tlmCellIdRepository = tlmCellIdRepository;
    }

    @Override
    public String cityFor(String cgi) {
        if (cgi == null || cgi.isBlank()) {
            return null;
        }
        return tlmCellIdRepository.findById(cgi)
                .map(TlmCellId::getVille)
                .filter(ville -> ville != null && !ville.isBlank())
                .orElse(null);
    }
}
