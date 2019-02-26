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
        if(vehOnRoad <= (road.getMaxCapacity() / 2)) {
        	
        	//logWriter.log(Level.INFO, "GRIDweightTimeAvg: returning default weight for road: " + road.getId() +
        	//                          " vehOnRoad was: " + vehOnRoad);
            return road.getLength()/currentSpeed;
        }
        else {
        	//logWriter.log(Level.INFO, "GRIDweightTimeAvg: returning mod weight for " + road.getId() + 
        	//		                  " vehOnRoad was: " + vehOnRoad +
        	//		                  " road max cap was: " + road.getMaxCapacity());

            return (road.getLength()/(currentSpeed) * 1.2);
        }
	}
}
