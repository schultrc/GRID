package edu.ucdenver.cse.GRIDmap;


public class GRIDnodeWtTmEm {
    private double nodeWtTotal;
    private long nodeTmTotal;
    private double nodeEmissions;

    public void setNodeWtTotal(double inWeight){ this.nodeWtTotal = inWeight; }
    public void setNodeTmTotal(long inTime){ this.nodeTmTotal = inTime; }
    public void setNodeEmissions(double inEmissions){ this.nodeEmissions = inEmissions; }
    
    public double getNodeWtTotal(){ return this.nodeWtTotal; }
    public long getNodeTmTotal(){ return this.nodeTmTotal; }
    public double getNodeEmissions(){ return this.nodeEmissions; }
}