package edu.ucdenver.cse.GRIDtime;

import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDroad;

public class GRIDtime {
    //final GRIDmap theMap;
    private double MAX_WEIGHT = 2000000.0;

    /*public GRIDcalcSpeed(GRIDmap map) {
        this.theMap = map;
    }*/

    public double updateEndTime(GRIDroad road, long startTime){

        if(road.equals(null)) {
            // THIS IS BAD DO SOMETHING
            logWriter.log(Level.WARNING, "NULL ROAD FOUND, exiting");
            System.out.println("\nEXITING\n");
            return MAX_WEIGHT;
        }

        double currentSpeed = road.getCurrentSpeed();

        if(currentSpeed == 0)
            return MAX_WEIGHT;

        long travelTime = (long) (road.getLength()/currentSpeed);

        return startTime + travelTime;
    }

    //public long getEndTime()
}
