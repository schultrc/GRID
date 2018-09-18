package edu.ucdenver.cse.GRIDweight;

import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDroad;

public class GRIDcalcSpeed {
    final GRIDmap theMap;
    double MAX_WEIGHT = 2000000.0;

    public GRIDcalcSpeed(GRIDmap map) {
        this.theMap = map;
    }

    public double calcSpeed(GRIDroad road, long startTime){

        // calc currentSpeed
        //GRIDroad road = theMap.hasRoad(fromNode, toNode);

        if(road.equals(null)) {
            // THIS IS BAD DO SOMETHING
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

    //public long getEndTime()
}
