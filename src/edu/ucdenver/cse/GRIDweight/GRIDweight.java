package edu.ucdenver.cse.GRIDweight;

public interface GRIDweight {
    public double emissions = 0.0;          // current emission level on the road
    public double idealSpeedLow = 15.8333;  // 15.8333 (~35 mph) to
    public double idealSpeedHigh = 24.4444; // 24.4444 (~55 mph) in meters per second
    public double MAX_WEIGHT = 2000000.0;

    double calcWeight(double currentSpeed, double roadLength);
}
