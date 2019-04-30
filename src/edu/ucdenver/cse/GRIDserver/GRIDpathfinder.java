package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteSegment;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDweight.*;
import java.util.logging.Level;
import java.util.concurrent.*;
import java.util.*;

public class GRIDpathfinder {
    private GRIDmap ourMap;
    
// This is the modified implementation of Dijkstra’s shortest path algorithm
    
    private List<String> visitedIntersections;
    private ConcurrentHashMap<String, GRIDrouteSegment> routeSegments;

    private GRIDweight theWeighter;

    public GRIDpathfinder(GRIDmap theMap, String weightType) {
    	this.ourMap = theMap;
    	
    	visitedIntersections = new Vector<String>(theMap.getIntersectionIDs().size());
        routeSegments = new ConcurrentHashMap<String, GRIDrouteSegment>();

        if (weightType.equalsIgnoreCase("TEST")) {
        	logWriter.log(Level.INFO, "Alternate weight function: TEST selected");

        	// CHANGE HERE TO USE YOUR WEIGHT FUNCTION
        	theWeighter = new GRIDweightTimeAvg(ourMap);
        }
        
        else {
        	// If no valid weighting class is selected, use the default:
        	theWeighter = new GRIDweightTimeAvg(ourMap);
        }       
    }
    
    public void init() {
    	// Set things up here

    }

    public GRIDroute findPath(GRIDagent thisAgent, long currentTime) {
        GRIDfibHeap pq = new GRIDfibHeap();

        // Keep a map of all of the fibHeap entries along with the intersectionID associated with them
        Map<String, GRIDfibHeap.Entry> fibEntryList = new HashMap<>();
        GRIDnode startNodeValues;
        String agentFrom; 
        String agentTo;
        String agentID;

        // The agent is already on the link, so we need its endpoint
        agentID = thisAgent.getId();
        		     
        agentFrom = ourMap.getRoad(thisAgent.getCurrentLink()).getTo();
        
        /* The agent will end somewhere on the final link, so we need to get to its "from end"
         */
        agentTo = ourMap.getRoad(thisAgent.getDestination()).getFrom();

        startNodeValues = new GRIDnode();
        startNodeValues.setNodeWeighttTotal(0.0);
        startNodeValues.setNodeTimeTotal(currentTime);
        GRIDnode tempNode = startNodeValues;

        if (agentTo.equals(agentFrom)) {
        	
        	// RCS clean this up
        	logWriter.log(Level.WARNING, "Agent: " + thisAgent.getId() + " already at destination: " + agentTo);
        	return genDummyRoute("ARRIVED");
        }
        
        Iterator<String> iter = ourMap.getIntersectionIDs().iterator();
        while (iter.hasNext()) {
            String intersectionID = iter.next();
            
            // enqueue each Node (intersection) into the fibHeap, and then keep a reference to it in entries
        	fibEntryList.put(intersectionID, pq.enqueue(intersectionID, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0L));
        }
        
        // Set the current location to be the first selected by the queue
        pq.decreaseKey(fibEntryList.get(agentFrom), 0.0, 0.0, currentTime);

        // prime the while loop with the start node, which is the starting min
        GRIDfibHeap.Entry currFibEntry = pq.dequeueMin();
                
        double calcWeight;
        GRIDrouteSegment tempSegment = null; 

        long arrivalTime;
    	double arrivalWeight;
        
    	logWriter.log(Level.INFO, "Starting route for: " + agentID +
    			                  " from: " + agentFrom +
    			                  " to: "   + agentTo +
    			                  " at time: " + currentTime);
    			                  
        while (!pq.isEmpty())
        {
        	// Add the intersection that we have visited
        	visitedIntersections.add(currFibEntry.getValue());
        	
            // step through every road leaving this intersection and 
            // update the priorities/weights of all of its edges.           
            for (String arc : ourMap.reachableDestinations(currFibEntry.getValue())) {
                
            	// skip this intersection if we've already visited it
            	if (visitedIntersections.contains(arc)) {
              		continue;
            	}

            	// Keep track of the time and total weight at this intersection
            	arrivalTime   = currFibEntry.getTmTotal();
            	arrivalWeight = currFibEntry.getWtTotal();
            	
                GRIDroad curRoad = ourMap.hasRoad(currFibEntry.getValue(), arc);

                if (curRoad.equals(null)) {
                	logWriter.log(Level.WARNING, "Unable to find road from: " + currFibEntry.getValue()+
                			                     " to: " + arc);
                	continue;
                }
            	                                   	
            	// Get the weight from the current node to the proposed node
                calcWeight = theWeighter.calcWeight(currFibEntry.getValue(), 
                		                            arc,
                                                    arrivalTime);

            	long traversalTime = curRoad.getTravelTime(currFibEntry.getTmTotal());

                /* If the weight of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */
                GRIDfibHeap.Entry dest = fibEntryList.get(arc);

                double newWeight = calcWeight + arrivalWeight;
                		
                // If we can get to dest for less than the previous best, we want to use this route
                if (newWeight < dest.getWtTotal())
                {
                    long destArrivalTime = traversalTime + arrivalTime;

                    pq.decreaseKey(dest, 0D, newWeight, destArrivalTime);
                                
                    tempSegment = new GRIDrouteSegment();
                    
                    tempSegment.setRoad_ID(curRoad.getId());
                    tempSegment.setStartIntersection(currFibEntry.getValue());
                    tempSegment.setEndIntersection(dest.getValue());
                    tempSegment.setTimeAtRoadEntry(arrivalTime);
                    tempSegment.setTimeAtRoadExit(destArrivalTime);
                
		            if(tempSegment != null) {
		            	// key these by their destination intersection, so we can build the route later
		            	routeSegments.put(tempSegment.getEndIntersection(), tempSegment);
		            }
		        }
            }  // end every road leaving current intersection
            
            /* Grab the current node.  The algorithm guarantees that we now
             * have the lowest weight to it.
             */
            currFibEntry = pq.dequeueMin();
            tempNode.setNodeTimeTotal(currFibEntry.getTmTotal());

        }  // while (!pq.isEmpty())
                 
        ConcurrentHashMap<String, GRIDrouteSegment> routeSegmentsByStart = new ConcurrentHashMap<String, GRIDrouteSegment>();
        // Now that we have all the segments, build the list so that we can find them easily
        
        for (Map.Entry<String, GRIDrouteSegment> theSegment : routeSegments.entrySet() ) {
        	
        	routeSegmentsByStart.put(theSegment.getValue().getStartIntersection(), theSegment.getValue());
        }
                                                           
        GRIDroute finalRoute = new GRIDroute();
        finalRoute.setAgent_ID(agentID);
        
        // Start with the destination and build the route recursively
        
        if(!routeSegments.containsKey(agentTo)){
            logWriter.log(Level.WARNING, "Agent " + agentID + " is going to: " + agentTo + " - but that doesn't exist in the returned list");
            System.out.println("Agent " + agentID + " is going to: " + agentTo + " - but that doesn't exist in the returned list");
        	return genDummyRoute("Destination unreachable");
        }
        	
        tempSegment = (GRIDrouteSegment) routeSegments.get(agentTo);
        
        if( tempSegment == null) {
        	logWriter.log(Level.WARNING, "Destination intersection not found in route! from was: " + agentFrom + " dest was: " + agentTo);
        	return genDummyRoute("Destination unreachable");
        }
        
        // Put the final segment into the route
        finalRoute.pushSegment(tempSegment);
        
        // If this is the only segment needed for the route
        if (tempSegment.getStartIntersection().equals(agentFrom)) {
        	//System.out.println("Agent " + agentID + " only has 1 leg in it's route" );
        	logWriter.log(Level.INFO, "GRIDpathfinder::findPath - Agent " + agentID + " only has 1 leg in it's route - the next step should be arrival" );
        }
        
        else {
	        boolean routeComplete = false;
	        while(!routeComplete) {
	        	// there should be only one segment in the collection that ends at the start point of the first segment of the route        	
	        	tempSegment = (GRIDrouteSegment) routeSegments.get(tempSegment.getStartIntersection());
	        		        	
	        	// This MAY be the place where we are already at our route - I.E. we left from the s
	        	if( tempSegment == null) {
        		
	            	logWriter.log(Level.WARNING, "GRIDpathfinder::findPath - Destination intersection not found in route for agent: " +
	        	                                 agentID + " from: " + agentFrom + " to: " + agentTo);
	            	return genDummyRoute("Destination unreachable");
	            }
	            
	            // Put the final segment into the route
	            finalRoute.pushSegment(tempSegment);
	            
	            if (tempSegment.getStartIntersection().equals(agentFrom)) {
	            	// This is the end case
	            	routeComplete = true;            	
	            }
	        }
        }
        
        logWriter.log(Level.INFO, "calculated route for agent: " + thisAgent +
        		                  " from: " + agentFrom + 
        		                  " to: " + agentTo + "is: " + finalRoute.toString());
        return finalRoute;
    }

    // Generate a dummy route. This is used when we fail computing a valid route, and want to not return null
    GRIDroute genDummyRoute(String reasonCode) {
    	GRIDroute dummyRoute = new GRIDroute();
    	dummyRoute.setAgent_ID(reasonCode);
    	return dummyRoute;
    }
   
/* END GRIDpathfinder CLASS */
}

