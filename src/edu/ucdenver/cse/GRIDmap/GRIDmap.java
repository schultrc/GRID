package edu.ucdenver.cse.GRIDmap;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteSegment;
import edu.ucdenver.cse.GRIDcommon.logWriter;

public final class GRIDmap {
	
	private ConcurrentMap<String, GRIDintersection> Intersections;
	private ConcurrentMap<String, GRIDroad> Roads;
	private ConcurrentMap<String, GRIDroad> roadList;
	
		//private ConcurrentMap<String, Long> intersectionList;

	public GRIDmap() {
		this.Intersections    = new ConcurrentHashMap<String, GRIDintersection>();
		this.Roads            = new ConcurrentHashMap<String, GRIDroad >();
		this.roadList         = new ConcurrentHashMap<String, GRIDroad>();
		//this.intersectionList = new ConcurrentHashMap<>();
	}
	
	public ConcurrentMap<String, GRIDintersection> getIntersections() {
		return Intersections;
	}
	
	public Set<String> getIntersectionIDs() {
		return Intersections.keySet();
	}
	public void setIntersections(ConcurrentMap<String, GRIDintersection> intersections) {
		Intersections = intersections;
	}
	public ConcurrentMap<String, GRIDroad > getRoads() {
		return Roads;
	}
	public void setRoads(ConcurrentMap<String, GRIDroad > roads) {
		Roads = roads;
	}
  
    public ConcurrentMap<String, GRIDroad> getRoadList() { return this.roadList; }

    public GRIDroad getRoadListItem(String itemID) { return roadList.get(itemID); }
    
    //public Long getIntersectionListItem(String itemID) { return intersectionList.get(itemID); }
	
	@Override
	public String toString() {
		return "GRIDmap [Intersections=" + Intersections + ", Roads=" + Roads + "]";
	}

	public boolean addRoad(GRIDroad addMe)
	{		
		// Only add a road if it isn't already in the map
		if(this.Roads.containsKey(addMe.getId())) {
			System.out.println("Road ID " + addMe.getId() + " already exists");
			return false;
		}
		else
		{
			this.Roads.put(addMe.getId(), addMe);
			//System.out.println("Successfully added road: " + addMe.getId());
		}
		
		return true;
	}
	
	public boolean addIntersection(GRIDintersection addMe) {
		if(this.Intersections.containsKey(addMe.getId())) {
			System.out.printf("Road ID %d already exists", addMe.getId());
			return false;
		}
		else {
			this.Intersections.putIfAbsent(addMe.getId(), addMe);
		}
		
		return true;			
	}
	
	public GRIDroad getRoad(String theRoadID) {
		
		return this.Roads.get(theRoadID);
	}
	
	public GRIDintersection getIntersection(String theIntersection) {
		return this.Intersections.get(theIntersection);
	}
	
	// determine if we have a road that goes from "from" to "to"
	// IF the road exists, return it. If not, return null
	public GRIDroad hasRoad(String from, String to) {
		return roadList.getOrDefault(from + "#" + to, null);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// The following section contains all the methods to use this map as a SERVER map
	// This probably makes sense to move into a child class
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// Master method to call all methods needed to set this map up as a SERVER map
	public boolean setupMapAsServer() {
		
		// Search by intersection name
		this.initMapSearchRoadsByIntersections();
		
		// Build the list of roads leaving an intersection
		this.setupDestinations();

		return true;
	}
	
	// This will create a searchable mapping of the roads in the map
	// this only needs to be called if the map needs to be rapidly searched by intersection names
	private void initMapSearchRoadsByIntersections() {
		
		// We need a way to get the road from the names of the intersections it resides on
		this.Roads.forEach((item,value)->
                roadList.put(value.getFrom() + "#" + value.getTo(), value));
    }

	// Only call this if the current instance of this map is being used as a server map (fib heap)
	private void setupDestinations() {
		
		for (GRIDroad theRoad : this.Roads.values()) {
			// Verify the corresponding intersection exists
			GRIDintersection tempIntersection = this.Intersections.get(theRoad.getFrom());
			
			if (tempIntersection == null) {
				logWriter.log(Level.WARNING, "Unable to add destination to: " + theRoad.getId());
			}
			
			tempIntersection.addDestination(theRoad.getTo(), theRoad.getLength());		
		}
	}
	
	// This will add the agents at the time they enter the road through the expected travel time
	public void updateMapWithAgents(GRIDroute theRoute) {

		long i;
		
		for (GRIDrouteSegment theSegment : theRoute.getRouteSegments()) {
			// Add vehicle count to the roads
			for (i = theSegment.getTimeAtRoadEntry(); i < theSegment.getTimeAtRoadExit(); i++) {

				this.getRoad(theSegment.getRoadID()).addAgentsToRoadAtTime(i);
				
			}
		}
	}

	// This will remove agents from the map starting at the entry time
	public void removeAgentsFromMap(GRIDroute theRoute, long previousSegmentEndTime) {

		long i;
		
		for (GRIDrouteSegment theSegment : theRoute.getRouteSegments()) {
			// Remove vehicles from the roads
			for (i = theSegment.getTimeAtRoadEntry(); i < theSegment.getTimeAtRoadExit(); i++) {
				// This isn't correct, but we aren't running matsim with a real
				// now time
				// Should have timeNow added to i
				this.getRoad(theSegment.getRoadID()).subFromWeight(i + previousSegmentEndTime);
			}
			
			previousSegmentEndTime += (i - 1);
			//logWriter.log(Level.INFO, "setting previousSegmentEndTime to: " + previousSegmentEndTime);
		}
	}

	// Return the map of roads that leave the supplied intersection name
	public List<String> reachableDestinations (String intersectionName) {
		
		// RCS this bombs if nothing is reachable
		List<String> intersectionsWeCanSee = this.Intersections.get(intersectionName).getIntersectionsFrom();
		
		if (intersectionsWeCanSee != null) {
			return intersectionsWeCanSee;
		}
		
		return null;
	}
}	
