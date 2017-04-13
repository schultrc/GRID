package edu.ucdenver.cse.GRIDcommon;

import edu.ucdenver.cse.GRIDcommon.logWriter;

/* routeSegments are the individual pieces that make up a route. 
 * 
 * RCS DEFINE PARAMETERS AND PURPOSE
 * 
 * 
 * 
 */

public class GRIDrouteSegment {
    private String road_ID;
    
    // RCS Maybe not needed?
    private String startIntersection;
    private String endIntersection;
    
    
    private long timeAtRoadEntry;
    private long timeAtRoadExit;

    // Default constructor
    public GRIDrouteSegment() {
    	this.road_ID           = "NONE";
    	this.startIntersection = "NONE";
    	this.endIntersection   = "NONE";
    	this.timeAtRoadEntry   = -1;
    	this.timeAtRoadExit    = -1;
    }
    
    public GRIDrouteSegment(String inputRoadID, Long exitTime ) {
        this.road_ID           = inputRoadID;
        this.startIntersection = "NONE";
        this.endIntersection   = "NONE";
        this.timeAtRoadEntry   = -1;
        this.timeAtRoadExit    = exitTime;
    }
    
    // RCS Other constructors????

    public GRIDrouteSegment(String road_ID, String startIntersection, 
    		                String endIntersection, long timeAtRoadEntry,
			                long timeAtRoadExit) {
    	
		this.road_ID = road_ID;
		this.startIntersection = startIntersection;
		this.endIntersection   = endIntersection;
		this.timeAtRoadEntry   = timeAtRoadEntry;
		this.timeAtRoadExit    = timeAtRoadExit;
	}
    
    public GRIDrouteSegment(String startIntersection, String endIntersection) {
    	this.startIntersection = startIntersection;
		this.endIntersection   = endIntersection;
    }

	public String getRoadID() { return road_ID; }
    public long getTimeAtRoadExit() { return timeAtRoadExit; }

    public void setRoadID(String inputRoadID) { this.road_ID = inputRoadID; }
    public void setTimeAtRoadExit(long inputStartTime) { this.timeAtRoadExit = inputStartTime; }

    public String toString() {
        return this.road_ID + " (time at exit: "+this.timeAtRoadExit;
    }
	public long getTimeAtRoadEntry() {
		return timeAtRoadEntry;
	}
	public void setTimeAtRoadEntry(long timeAtRoadEntry) {
		this.timeAtRoadEntry = timeAtRoadEntry;
	}
	
	public long getTravelTime () {
		if ((this.timeAtRoadEntry == -1) || (this.timeAtRoadExit == -1)) {
			logWriter.log(java.util.logging.Level.WARNING, "Attempt to get travel time for " +
		                  "route segment with uninitialized values");
			return -1;
		}
		
		return (this.timeAtRoadExit - this.timeAtRoadEntry);
	}
}
