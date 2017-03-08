package edu.ucdenver.cse.GRIDcommon;

import java.util.ArrayList;
import java.io.Serializable;

public class GRIDroute implements Serializable {	
	/**
	 * a route is a collection of route segments, in order, that detail the path
	 * an agent will take through the map
	 */
	private static final long serialVersionUID = 2L;
	private Long agent_ID;
	
	private ArrayList<GRIDrouteSegment> RouteSegments;
	
	public GRIDroute() {
		//Intersections = new ArrayList<String>(2);
		//Roads = new ArrayList<String>(2);
		this.RouteSegments = new ArrayList<GRIDrouteSegment>();
	}
	
	public GRIDroute(ArrayList<GRIDrouteSegment> theRoute) {
		this.RouteSegments = theRoute;
	}
	
	// Add a new route segment with only the end intersection
	public void addSegmentByIntersection(String newIntersection){
		// make sure this isn't the first segment in the route
		
		// use the previous segment's destination as the start for this segment
		
		
	}
	
	
	
	//public ArrayList<String> getIntersections(){ return this.Intersections; }
	public Long getAgent_ID(){ return this.agent_ID; }
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
