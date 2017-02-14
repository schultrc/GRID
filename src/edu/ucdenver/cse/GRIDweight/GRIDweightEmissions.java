package edu.ucdenver.cse.GRIDweight;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;

public class GRIDemissions implements GRIDweight {

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
            //return (roadLength/idealSpeedHigh + (currentSpeed-idealSpeedHigh));
            return Double.POSITIVE_INFINITY;
        }

        //System.out.println("just right");
        return roadLength/idealSpeedLow + currentSpeed-idealSpeedLow;
    }

    public GRIDroute resetEmissionsForAgent() {
        GRIDroute newRoute = new GRIDroute();
        return newRoute;
    }
}
