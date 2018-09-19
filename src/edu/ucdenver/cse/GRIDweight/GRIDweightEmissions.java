package edu.ucdenver.cse.GRIDweight;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDmap.GRIDroad;

public class GRIDweightEmissions implements GRIDweight {

    public double calcWeight(double in, double out){ return 0.0; }

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
        else if(currentSpeed > idealSpeedHigh){
            //System.out.println("too fast");
            //return (roadLength/idealSpeedHigh + (currentSpeed-idealSpeedHigh));
            return Double.POSITIVE_INFINITY;
        }
        else{
            //System.out.println("just right");
            return roadLength/idealSpeedLow + currentSpeed-idealSpeedLow;
        }
    }

    public GRIDroute resetEmissionsForAgent() {
        GRIDroute newRoute = new GRIDroute();
        return newRoute;
    }

	@Override
	public double calcWeight(GRIDroad road, long startTime) {
		// TODO Auto-generated method stub
		return 0;
	}
}
