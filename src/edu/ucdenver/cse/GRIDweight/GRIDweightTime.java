package edu.ucdenver.cse.GRIDweight;

public class GRIDweightTime implements GRIDweight {

    public double calcWeight(double in, double out){
        return 0.0;
    }

    public double calcTimeWeight(double currentSpeed, double roadLength,
                                 double capMinusActual){
        if(currentSpeed == 0)
            return MAX_WEIGHT;

        //calcCurrentSpeed(intervalStartTime);

        if(capMinusActual <= 0.0) {
            return roadLength/currentSpeed;
        }
        else {
            return roadLength/(currentSpeed*capMinusActual);
        }
    }

}
