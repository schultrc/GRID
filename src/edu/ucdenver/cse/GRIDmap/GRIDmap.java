package edu.ucdenver.cse.GRIDmap;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteSegment;
import edu.ucdenver.cse.GRIDcommon.logWriter;


public class GRIDmap implements Iterable<String> {

	private Map<String, Map<String, Double>> mGraph;
	private ConcurrentMap<String, GRIDintersection> Intersections;
	private ConcurrentMap<String, GRIDroad> Roads;
	private ConcurrentMap<String, GRIDroad> roadList;
	private ConcurrentMap<String, Long> intersectionList;

	public GRIDmap() {
		this.mGraph = new HashMap<String, Map<String, Double>>();
		this.Intersections    = new ConcurrentHashMap<String, GRIDintersection>();
		this.Roads            = new ConcurrentHashMap<String, GRIDroad >();
		this.roadList         = new ConcurrentHashMap<String, GRIDroad>();
		this.intersectionList = new ConcurrentHashMap<>();
	}
	
	public ConcurrentMap<String, GRIDintersection> getIntersections() {
		return Intersections;
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
  
	// Once the map has been created and filled in, we need to do some final processing
	// This will create a searchable list of roads by combining the start and destination
	// intersection names
	
	public void initMap() {
		this.Roads.forEach((item,value)->
                roadList.put(value.getFrom()+"#"+value.getTo(),value));
		
		// RCS I don't think we need the intersections any more
		//this.Intersections.forEach((item,value)->
		//		intersectionList.put(value.getId(),0L));
    }

    //public ConcurrentMap<String, GRIDroad> getRoadList() { return this.roadList; }

    //public GRIDroad getRoadListItem(String itemID) { return roadList.get(itemID); }
    
    //public Long getIntersectionListItem(String itemID) { return intersectionList.get(itemID); }
	//public void setIntersectionListItemTimeAtExit(String itemID, Long exitTime) {
		//GRIDintersection tempIntersection = new GRIDintersection();
	//}
	
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
		// Old Break Point
		
		//logWriter.log(Level.INFO, "Attempting to find road: " + theRoadID);
		
		return this.Roads.get(theRoadID);
		//return roadList.get(theRoadID);
	}
	
	public GRIDintersection getIntersection(String theIntersection) {
		return this.Intersections.get(theIntersection);
	}
	
	// determine if we have a road that goes from "from" to "to"
	// IF the road exists, return it. If not, return null
	public GRIDroad hasRoad(String from, String to) {

		return roadList.getOrDefault(from+"#"+to, null);
	}

	public void updateMapWithAgents(GRIDroute theRoute, long previousSegmentEndTime) {

		long i;
		
		for (GRIDrouteSegment theSegment : theRoute.getRouteSegments()) {
			// Add vehicle count to the roads
			for (i = 0L; i < theSegment.getTravelTime(); i++) {

				this.getRoad(theSegment.getRoadID()).addAgentsToRoadAtTime(i + previousSegmentEndTime);
				
				//logWriter.log(Level.INFO, "weight on road: " + ourRoad +
				//		                  "for i value: "    + i +
				//		                  " at time: "       + (i+previousSegmentEndTime) +
				//		                  " increased to: "  + this.getRoad(ourRoad).getWeightAtTime(i + previousSegmentEndTime));
			}
			
			previousSegmentEndTime += (i - 1);
			//logWriter.log(Level.INFO, "setting previousSegmentEndTime to: " + previousSegmentEndTime);
		}
	}

	// 
	public void removeAgentsFromMap(GRIDroute theRoute, long previousSegmentEndTime) {

		long i;
		
		for (GRIDrouteSegment theSegment : theRoute.getRouteSegments()) {
			// Remove vehicles from the roads
			for (i = 0L; i < theSegment.getTravelTime(); i++) {
				// This isn't correct, but we aren't running matsim with a real
				// now time
				// Should have timeNow added to i
				this.getRoad(theSegment.getRoadID()).subFromWeight(i + previousSegmentEndTime);
			}
			
			previousSegmentEndTime += (i - 1);
			//logWriter.log(Level.INFO, "setting previousSegmentEndTime to: " + previousSegmentEndTime);
		}
	}

	public GRIDmap.graphEdge getEdge(String start, String finish) // Map.Entry<String, Map.Entry<String, Double>>
	{
		GRIDmap.graphEdge temp = new GRIDmap.graphEdge();
		mGraph.containsKey(start);
		Map<String, Double> arcs = mGraph.get(start);
		for(String key : arcs.keySet())
		{
			if(key == finish)
			{
				temp.setStart(start);
				temp.setFinish(key);
				temp.setWeight(arcs.get(key));
				return temp;
			}
		}
		return null;
	}

	public boolean addNode(String node) {
		/* If the node already exists, don't do anything. */
		if (mGraph.containsKey(node))
			return false;

		/* Otherwise, add the node with an empty set of outgoing edges. */
		mGraph.put(node, new HashMap<String, Double>());
		return true;
	}

	public void addEdge(String start, String dest, double length) {
		/* Confirm both endpoints exist. */
		if (!mGraph.containsKey(start) || !mGraph.containsKey(dest))
			throw new NoSuchElementException("Both nodes must be in the graph.");

		/* Add the edge. */
		mGraph.get(start).put(dest, length);
	}

	public Map<String, Double> edgesFrom(String node) {
		/* Check that the node exists. */
		Map<String, Double> arcs = mGraph.get(node);
		if (arcs == null)
			throw new NoSuchElementException("Source node does not exist.");

		return Collections.unmodifiableMap(arcs);
	}

	public class graphEdge{
		String start;
		String finish;
		Double weight;

		public void setStart(String val){start = val;}
		public void setFinish(String val){finish = val;}
		public void setWeight(Double val){weight = val;}

		@Override
		public String toString(){return "Edge [start: "+start+" end: "+finish+" weight: "+weight+"]";}
	}

	public boolean loadEdges(GRIDmap myGraph) {
		Long startTime = System.nanoTime();

		ArrayList<String> networkIntersections = new ArrayList<>(myGraph.getIntersections().keySet());
		ArrayList<GRIDroad> networkRoads = new ArrayList<>(myGraph.getRoads().values());

		// replace with better for loop--how dare you criticize my loop!
		for(int i = 0; i < networkIntersections.size(); i++)
		{
			addNode(networkIntersections.get(i));
		}

		for(int i = 0; i < networkRoads.size(); i++)
		{
			/*MFS REMOVE the system output line just below here*/
			//System.out.println(networkRoads.get(i) + "road " + (i+1));
			//if(i+1 == networkRoads.size()){System.out.println("edges: "+(i+1));}
			addEdge(networkRoads.get(i).getFrom(), networkRoads.get(i).getTo(), networkRoads.get(i).getLength());
		}

		//return myGraph;

		return true;
	}

	public Iterator<String> iterator() {
		return mGraph.keySet().iterator();
	}
}

