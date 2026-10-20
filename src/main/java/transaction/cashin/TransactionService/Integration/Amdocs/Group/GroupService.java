package transaction.cashin.TransactionService.Integration.Amdocs.Group;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    private final ProducerTemplate producerTemplate;

    public GroupService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public GroupResponse getGroup(GroupRequest request, String username, String password) {
        return producerTemplate.requestBodyAndHeader(
                "direct:amdocs-get-group",
                request,
                "Authorization",
                basicAuthorization(username, password),
                GroupResponse.class
        );
    }

    public GroupResponse getGroup(
            GroupRequest request)
          //  String accessToken) 
    {

        return producerTemplate.requestBody(
                "direct:amdocs-get-group",
                request,
            //    "Authorization",
            //    "Bearer " + accessToken,
                GroupResponse.class
        );
    }

    private String basicAuthorization(String username, String password) {
        String credentials = username + ":" + (password == null ? "" : password);
        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }
}
