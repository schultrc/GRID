package edu.ucdenver.cse.GRIDweight;

public interface GRIDweight {
	// defaults
    public double idealSpeedLow = 15.8333;  // 15.8333 (~35 mph) to
    public double idealSpeedHigh = 24.4444; // 24.4444 (~55 mph) in meters per second
    public double MAX_WEIGHT = 2000000.0;   // High enough to alway outweigh any other value
    
    // Must return the weight of traveling from the fromNode to the toNode, starting at time startTime
    double calcWeight(String fromNode, String toNode, long startTime );
    
}
