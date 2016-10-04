package edu.ucdenver.cse.GRIDmap;


public class GRIDnodeWeightTime {
    private Double nodeWtTotal;
    private Long nodeTmTotal;

    public void setNodeWtTotal(Double inWeight){ this.nodeWtTotal = inWeight; }
    public void setNodeTmTotal(Long inTime){ this.nodeTmTotal = inTime; }

    public Double getNodeWtTotal(){ return this.nodeWtTotal; }
    public Long getNodeTmTotal(){ return this.nodeTmTotal; }
}

