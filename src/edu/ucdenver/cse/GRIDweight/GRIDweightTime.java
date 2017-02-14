package edu.ucdenver.cse.GRIDweight;

public class GRIDtime implements GRIDweight {

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
