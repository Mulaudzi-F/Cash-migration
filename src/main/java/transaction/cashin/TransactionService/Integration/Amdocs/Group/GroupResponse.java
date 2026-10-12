package transaction.cashin.TransactionService.Integration.Amdocs.Group;

import java.util.List;

public class GroupResponse {

    private List<AgentGroup> agentGroups;

    public GroupResponse() {
    }

    public List<AgentGroup> getAgentGroups() {
        return agentGroups;
    }

    public void setAgentGroups(List<AgentGroup> agentGroups) {
        this.agentGroups = agentGroups;
    }
}