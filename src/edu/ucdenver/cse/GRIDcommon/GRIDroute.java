package edu.ucdenver.cse.GRIDcommon;

import java.util.ArrayList;
//import java.util.Collections;
import java.io.Serializable;

public class GRIDroute implements Serializable {	
	/**
	 * a route is a collection of route segments, in order, that detail the path
	 * an agent will take through the map
	 */
	private static final long serialVersionUID = 2L;
	private String agent_ID;
	
	

	private ArrayList<GRIDrouteSegment> RouteSegments;
	
	public GRIDroute() {
		//Intersections = new ArrayList<String>(2);
		//Roads = new ArrayList<String>(2);
		
		// Consider making this a syncronizedList to allow for multi-threading
		// this.RouteSegments = (ArrayList<GRIDrouteSegment>) Collections.synchronizedList(new ArrayList<GRIDrouteSegment>());
		this.RouteSegments = new ArrayList<GRIDrouteSegment>();
	}
	
	public GRIDroute(ArrayList<GRIDrouteSegment> theRoute) {
		this.RouteSegments = theRoute;
	}
	
	public void addSegment(GRIDrouteSegment theSegment) {
		this.RouteSegments.add(theSegment);
	}
	
	// Add a new route segment with only the end intersection
	public void addSegmentByIntersection(String newIntersection){
		// make sure this isn't the first segment in the route
		
		// use the previous segment's destination as the start for this segment
	}
	
	public void setAgent_ID(String agent_ID) {
		this.agent_ID = agent_ID;
	}
	
	//public ArrayList<String> getIntersections(){ return this.Intersections; }
	public String getAgent_ID(){ return this.agent_ID; }
	
	//public ArrayList<String> getRoads() {return this.Roads; }
	//public void setRoads(ArrayList<String> theRoads) { this.Roads = theRoads; }

	public long getcalculatedTravelTime(){ 
		long travelTime = 0;

		// RCS FIX THIS TO DO SOMETHING
		// last segment end minus first segment start?
		return travelTime;
	}

	public ArrayList<GRIDrouteSegment> getRouteSegments() { return this.RouteSegments; }
	public void setRouteSegments(ArrayList<GRIDrouteSegment> theRouteSegments)
								{ this.RouteSegments = theRouteSegments; }
	
	public boolean compare(GRIDroute newRoute) {
		if (this.RouteSegments.isEmpty() || newRoute.RouteSegments.isEmpty()) { return false; }
		if (this.RouteSegments.size() != newRoute.RouteSegments.size()) { return false; }

		for(int i = 0; i < this.RouteSegments.size(); ++i) {
			if (!this.RouteSegments.get(i).getRoadID().equals(newRoute.RouteSegments.get(i).getRoadID())) { 
				return false; 
			}
		}

		return true;
	}
	
	public ArrayList<String> getRoads() {
		ArrayList<String> theRoads = new ArrayList<String>();
	
		for(GRIDrouteSegment theSegment : this.RouteSegments) {
			theRoads.add(theSegment.getRoadID());
		}
		
		return theRoads;
	}
	
	public String toString() {
		String theRouteStr = "Route: ";
		
		for(GRIDrouteSegment segment:this.RouteSegments ) {
			theRouteStr += " " + segment.toString();
		}
		
		return theRouteStr;
	}
}
