package edu.ucdenver.cse.GRID.GRID_ALGORITHM;
import edu.ucdenver.cse.GRID.MAP.*;

/**
 * Created by MFS on 9/25/2016.
 */
public class GRIDutilityFunction {

    private Double emissions;
    private Double idealSpeedLow; // meters per second
    private Double idealSpeedHigh;

    public GRIDutilityFunction(){
        emissions = 0.0;
        idealSpeedLow = 15.8333; // (~35 mph) to 24.4444 (~55 mph)
        idealSpeedHigh = 24.4444;
    }
    /*
	 * this is an approximation to compare the emissions for a given agent
	 */
    public double calcEmissions (Double currentSpeed, Double roadLength) {
        /* BEGIN test output */
        /*System.out.println("ideal time: "+roadLength/idealSpeed);
        System.out.println("roadLength: "+roadLength);
        System.out.println("current speed: "+currentSpeed);
        System.out.println("negative: "+(currentSpeed-idealSpeed));
        /* System.out.println("emissions: "+emissions);*/
        /* END test output */
        // emissions = roadLength/idealSpeed + (roadLength*(currentSpeed-idealSpeed));

        if(currentSpeed < idealSpeedLow){
            //System.out.println("too slow");
            return (roadLength/idealSpeedLow + (idealSpeedLow-currentSpeed));
        }
        if(currentSpeed > idealSpeedHigh){
            //System.out.println("too fast");
            return (roadLength/idealSpeedHigh + (currentSpeed-idealSpeedHigh));
        }

        //System.out.println("just right");
        return roadLength/idealSpeedLow + currentSpeed-idealSpeedLow;
    }

    public GRIDroute resetEmissionsForAgent() {
        GRIDroute newRoute = new GRIDroute();
        return newRoute;
    }
}
