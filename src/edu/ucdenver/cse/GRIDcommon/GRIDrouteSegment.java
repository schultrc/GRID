package edu.ucdenver.cse.GRIDcommon;

import java.io.Serializable;

import edu.ucdenver.cse.GRIDcommon.logWriter;

/* routeSegments are the individual pieces that make up a route. 
 */

public class GRIDrouteSegment implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String road_ID;
    
    // RCS Maybe not needed?
    private String startIntersection;
    private String endIntersection;
    private long timeAtRoadEntry;
    private long timeAtRoadExit;
    
    public String getRoad_ID() {
		return road_ID;
	}

	public void setRoad_ID(String road_ID) {
		this.road_ID = road_ID;
	}

	public String getStartIntersection() {
		return startIntersection;
	}

	public void setStartIntersection(String startIntersection) {
		this.startIntersection = startIntersection;
	}

	public String getEndIntersection() {
		return endIntersection;
	}

	public void setEndIntersection(String endIntersection) {
		this.endIntersection = endIntersection;
	}	

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
    // RCS probably want to get rid of most of these - or make sure they are unique
    
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

    public GRIDrouteSegment(String startIntersection, String endIntersection, String roadID) {
    	this.startIntersection = startIntersection;
		this.endIntersection   = endIntersection;
		this.road_ID = roadID;
    }
    
	public String getRoadID() { return road_ID; }
    public long getTimeAtRoadExit() { return timeAtRoadExit; }

    public void setRoadID(String inputRoadID) { this.road_ID = inputRoadID; }
    public void setTimeAtRoadExit(long inputStartTime) { this.timeAtRoadExit = inputStartTime; }

    public String toString() {
        //return this.road_ID + " (time at exit: "+this.timeAtRoadExit;
    	return this.road_ID;
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
			logWriter.log(java.util.logging.Level.WARNING, "Time at exit for routeSegment: " + this.getRoad_ID() + " is: " + this.timeAtRoadExit);
			return -1;
		}
		
		return (this.timeAtRoadExit - this.timeAtRoadEntry);
	}
}
