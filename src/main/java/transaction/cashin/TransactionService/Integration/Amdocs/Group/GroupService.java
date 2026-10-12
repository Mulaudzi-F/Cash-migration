package transaction.cashin.TransactionService.Integration.Amdocs.Group;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    private final ProducerTemplate producerTemplate;

    public GroupService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public GroupResponse getGroup(
            GroupRequest request,
            String accessToken) {

        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-get-group",
                request,
                "Authorization",
                "Bearer " + accessToken,
                GroupResponse.class
        );
    }
}
