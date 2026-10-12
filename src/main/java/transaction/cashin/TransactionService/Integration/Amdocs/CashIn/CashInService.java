package transaction.cashin.TransactionService.Integration.Amdocs.CashIn;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class CashInService {

    private final ProducerTemplate producerTemplate;

    public CashInService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public CashInResponse executeCashIn(
            CashInRequest request,
            String accessToken) {

        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-cashin",
                request,
                "Authorization",
                "Bearer " + accessToken,
                CashInResponse.class
        );
    }
}
