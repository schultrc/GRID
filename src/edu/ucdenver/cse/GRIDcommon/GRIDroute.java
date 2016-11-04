package edu.ucdenver.cse.GRIDcommon;

import java.util.ArrayList;
import java.io.Serializable;

public class GRIDroute implements Serializable {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long agent_ID;
	private Long calculatedTravelTime;
	private ArrayList<String> Intersections;
	private ArrayList<String> Roads;
	private ArrayList<GRIDrouteSegment> RouteSegments;
	
	public GRIDroute() {
		Intersections = new ArrayList<String>(2);
		Roads = new ArrayList<String>(2);
		RouteSegments = new ArrayList<GRIDrouteSegment>();
	}
	
	public void setCalculatedTravelTime(Long inTime){ this.calculatedTravelTime = inTime; }

	public ArrayList<String> getIntersections(){ return this.Intersections; }
	public Long getAgent_ID(){ return this.agent_ID; }
	public Long getcalculatedTravelTime(){ return this.calculatedTravelTime; }
	public ArrayList<String> getRoads() {return this.Roads; }
	public void setRoads(ArrayList<String> theRoads) { this.Roads = theRoads; }

	public void addRoad(String roadID) {
		this.Roads.add(roadID);
	}
	
	public void addIntersection(String intersectionID) {
		this.Intersections.add(intersectionID);
	}
	
	
	public ArrayList<GRIDrouteSegment> getRouteSegments() { return this.RouteSegments; }
	public void setRouteSegments(ArrayList<GRIDrouteSegment> theRouteSegments)
								{ this.RouteSegments = theRouteSegments; }
	
	
	public boolean equalsRoads(GRIDroute otherRoute) {
		if (this.Roads.isEmpty() || otherRoute.getRoads().isEmpty()) { return false; }
		if (this.Roads.size() != otherRoute.getRoads().size()) { return false; }
	
		if (this.RouteSegments.isEmpty() || otherRoute.getRouteSegments().isEmpty()) { return false; }
		if (this.RouteSegments.size() != otherRoute.getRouteSegments().size()) { return false; }
		
		for(int i = 0; i < this.Roads.size(); ++i) {
		   if (this.Roads.get(i) != otherRoute.getRoads().get(i)) { return false; }	
		}
		
		for(int i = 0; i < this.RouteSegments.size(); ++i) {
			if (this.RouteSegments.get(i).equals(otherRoute.getRouteSegments().get(i))) { return false; }
		}

		return true;
	}
	
	// FIX this? 
	public boolean equalsIntersections(GRIDroute otherRoute) {
		if (this.Intersections.isEmpty() || otherRoute.getIntersections().isEmpty()) { return false; }
		if (this.Intersections.size() != otherRoute.getIntersections().size()) { return false; }
	
		for(int i = 0; i < this.Intersections.size(); ++i) {
		   if (this.Intersections.get(i) != otherRoute.getIntersections().get(i)) { return false; }	
		}
		
		return true;
	}
	
	public boolean compareOldNewRoute(GRIDroute newRoute) {
		if (this.RouteSegments.isEmpty() || newRoute.RouteSegments.isEmpty()) { return false; }
		if (this.RouteSegments.size() != newRoute.RouteSegments.size()) { return false; }

		for(int i = 0; i < this.RouteSegments.size(); ++i) {
			if (!this.RouteSegments.get(i).equals(newRoute.RouteSegments.get(i))) { return false; }
		}

		return true;
	}
	public String toString() {
		String theRouteStr = "Route: ";
		
		for(String road:this.Roads) {
			theRouteStr += " " + road;
		}
		
		return theRouteStr;
	}

	public String toStringByInt() {
		String theRouteStr = "Route: ";
		
		for(String intersection:this.Intersections) {
			theRouteStr += " " + intersection;
		}
		
		return theRouteStr;
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Everything below is for emissions, moved here for clarity
	private Double calculatedEmissionsTotal;
	
	public void setCalculatedEmissionsTotal(){
		this.calculatedEmissionsTotal = 0.0;

		for(GRIDrouteSegment segment : RouteSegments) {
			this.calculatedEmissionsTotal += segment.getSegmentEmissions();
		}
	}
	public Double getCalculatedEmissionsTotal(){ return this.calculatedEmissionsTotal; }
	
	
	
	
	
	
	
	
	
	
	
	
}
