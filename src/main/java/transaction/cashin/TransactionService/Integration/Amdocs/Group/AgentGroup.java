package transaction.cashin.TransactionService.Integration.Amdocs.Group;

public class AgentGroup {

    private String name;
    private Integer type;
    private Integer agentGroupId;
    private String category;
    private String sourceIp;
    private Integer id;
    private Integer qosId;

    public AgentGroup() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getAgentGroupId() {
        return agentGroupId;
    }

    public void setAgentGroupId(Integer agentGroupId) {
        this.agentGroupId = agentGroupId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQosId() {
        return qosId;
    }

    public void setQosId(Integer qosId) {
        this.qosId = qosId;
    }
}
