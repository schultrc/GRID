package edu.ucdenver.cse.GRIDweight;

import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDroad;

public class GRIDweightTimeAvg implements GRIDweight{

	final GRIDmap theMap;
    
	public GRIDweightTimeAvg(GRIDmap map) {
		this.theMap = map;
	}

	// This uses 2 intersections as from and to. Should we pass in the roadID (or obj) instead?
    @Override
	public double calcWeight(String fromNode, String toNode, long startTime) {

    	// calc currentSpeed
    	GRIDroad road = theMap.hasRoad(fromNode, toNode);
    	
    	if(road.equals(null)) {
    		// THIS IS BAD DO SOMETHING
    		logWriter.log(Level.WARNING, "GRIDweightTimeAvg: NULL ROAD FOUND, exiting");
    		return MAX_WEIGHT;
    	}
    	
    	double currentSpeed = road.getCurrentSpeed();
    	
    	if(currentSpeed == 0)
            return MAX_WEIGHT;
    	
    	long travelTime = (long) (road.getLength()/currentSpeed);
    	
    	double vehOnRoad = road.getAvgVehicleCount(startTime, (startTime + travelTime));
        
    	//if(vehOnRoad <= (road.getMaxCapacity() / 2)) {
        if(vehOnRoad <= (road.getMaxCapacity() / 1.25)) {
            return (road.getLength()/currentSpeed);
        }
        else {
            return (road.getLength()/(currentSpeed) * 1.1);
        }
	}
}
