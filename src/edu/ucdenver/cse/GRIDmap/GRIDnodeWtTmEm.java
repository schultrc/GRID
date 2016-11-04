package edu.ucdenver.cse.GRIDmap;


public class GRIDnodeWtTmEm {
    private Double nodeWtTotal;
    private Long nodeTmTotal;
    private Double nodeEmissions;

    public void setNodeWtTotal(Double inWeight){ this.nodeWtTotal = inWeight; }
    public void setNodeTmTotal(Long inTime){ this.nodeTmTotal = inTime; }
    public void setNodeEmissions(Double inEmissions){ this.nodeEmissions = inEmissions; }
    
    public Double getNodeWtTotal(){ return this.nodeWtTotal; }
    public Long getNodeTmTotal(){ return this.nodeTmTotal; }
    public Double getNodeEmissions(){ return this.nodeEmissions; }
}

