package edu.ucdenver.cse.GRIDmap;

public class GRIDnode {
    private double nodeWeightTotal;
    private long nodeTimeTotal;

    public void setNodeWeighttTotal(double inWeight){ this.nodeWeightTotal = inWeight; }
    public void setNodeTimeTotal(long inTime){ this.nodeTimeTotal = inTime; }
    
    // add time to the existing value
    public void addNodeTime(long time) { this.nodeTimeTotal += time; }
    
    public double getNodeWeightTotal(){ return this.nodeWeightTotal; }
    public long getNodeTimeTotal(){ return this.nodeTimeTotal; }
    
    public String toString() {
    	
    	return "node weight: " + this.nodeWeightTotal + " node time: " + this.nodeTimeTotal; 
    }
}