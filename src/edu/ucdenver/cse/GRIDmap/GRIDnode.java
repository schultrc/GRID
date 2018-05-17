package edu.ucdenver.cse.GRIDmap;


public class GRIDnode {
    private double nodeWeightTotal;
    private long nodeTimeTotal;
    private long nodeTimeAtEntry;

    public void initNodeValues(long inTime, double inWeight){
        this.nodeTimeAtEntry = inTime;
        this.nodeTimeTotal = inTime;
        this.nodeWeightTotal = inWeight;
    }

    public void setNodeWeighttTotal(double inWeight){ this.nodeWeightTotal = inWeight; }
    public void setNodeTimeTotal(long inTime){ this.nodeTimeTotal = inTime; }
    public void setNodeEntryTime(long inTime){ this.nodeTimeAtEntry = inTime; }
    
    public double getNodeWeightTotal(){ return this.nodeWeightTotal; }
    public long getNodeTimeTotal(){ return this.nodeTimeTotal; }
    public long getNodeTimeAtEntry(){ return this.nodeTimeAtEntry; }
}