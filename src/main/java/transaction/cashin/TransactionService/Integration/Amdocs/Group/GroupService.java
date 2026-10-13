package transaction.cashin.TransactionService.Integration.Amdocs.Group;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    private final ProducerTemplate producerTemplate;

    @Value("${amdocs.authentication.username}")
    private String username;

    @Value("${amdocs.authentication.password}")
    private String password;

    public GroupService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public GroupResponse getGroup(GroupRequest request) {
        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-get-group",
                request,
                "Authorization",
                basicAuthorization(),
                GroupResponse.class
        );
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

    private String basicAuthorization() {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }
}
