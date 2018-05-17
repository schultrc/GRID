package edu.ucdenver.cse.GRIDweight;

import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.logWriter;
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

    	//if(road.equals(null)) {
		if(road == null) {
    		// THIS IS BAD DO SOMETHING
			System.out.println("GRIDwtTm--NULL ROAD FOUND, exiting...");
    		logWriter.log(Level.WARNING, "NULL ROAD FOUND, exiting");
    		return MAX_WEIGHT;
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
