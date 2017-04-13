package edu.ucdenver.cse.GRIDweight;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDroad;

public class GRIDweightTime implements GRIDweight {
	
	final GRIDmap theMap;
    
	public GRIDweightTime(GRIDmap map) {
		this.theMap = map;
	}

	// This uses 2 intersections as from and to. Should we pass in the roadID (or obj) instead?
    @Override
	public double calcWeight(String fromNode, String toNode, long startTime) {

    	// calc currentSpeed
    	GRIDroad road = theMap.hasRoad(fromNode, toNode);
    	
    	if(road.equals(null)) {
    		// THIS IS BAD DO SOMETHING
    	}
    	
    	double currentSpeed = road.getCurrentSpeed();
    	
    	if(currentSpeed == 0)
            return MAX_WEIGHT;
    	
    	long travelTime = (long) (road.getLength()/currentSpeed);
    	
    	double capMinusActual = road.getMaxCapacity() - road.getAvgVehicleCount(startTime, (startTime + travelTime));
        if(capMinusActual <= 0.0) {
            return road.getLength()/currentSpeed;
        }
        else {
            return road.getLength()/(currentSpeed*capMinusActual);
        }
	}
}
