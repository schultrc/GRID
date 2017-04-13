package edu.ucdenver.cse.GRIDmap;


public class GRIDnode {
    private double nodeWeightTotal;
    private long nodeTimeTotal;

    public void setNodeWeighttTotal(double inWeight){ this.nodeWeightTotal = inWeight; }
    public void setNodeTimeTotal(long inTime){ this.nodeTimeTotal = inTime; }
    
    public double getNodeWeightTotal(){ return this.nodeWeightTotal; }
    public long getNodeTimeTotal(){ return this.nodeTimeTotal; }
}