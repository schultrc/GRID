package edu.ucdenver.cse.GRIDweight;

import edu.ucdenver.cse.GRIDmap.GRIDmap;

public interface GRIDweight {
    public double idealSpeedLow = 15.8333;  // 15.8333 (~35 mph) to
    public double idealSpeedHigh = 24.4444; // 24.4444 (~55 mph) in meters per second
    public double MAX_WEIGHT = 2000000.0;
    
    // Must return the weight of traveling from the fromNode to the toNode, starting at time startTime
    double calcWeight(String fromNode, String toNode, long startTime );
    
}
