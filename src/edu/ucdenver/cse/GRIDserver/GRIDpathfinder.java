/*
    Code Citation:

    Original designer: Keith Schwarz, Stanford CS Dept.

    Using code from the following website as the basis for the Fibonacci heap
    data structure for our Dijkstra's implementation:

    http://www.keithschwarz.com/interesting/
    http://www.keithschwarz.com/interesting/code/?dir=fibonacci-heap
*/

package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteSegment;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDweight.*;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import java.util.logging.Level;


import java.util.concurrent.*;

import java.util.*;
import java.util.ListIterator;

public class GRIDpathfinder {
    private GRIDmap ourMap;
    
    private ConcurrentMap<String, GRIDnode> workingRoute;
    private ConcurrentHashMap<String, GRIDrouteSegment> routeSegments;

    private GRIDweight theWeighter;

    public GRIDpathfinder(GRIDmap theMap) {
    	this.ourMap = theMap;
    	
    	// RCS change to init the directed graph. MOVE TO INIT FUNCTION???
        // RCS I "Think" This is taken care of in the map
    	//graph = graphLoadEdges(theMap);
        
    	// What are these? ? ? ?
        workingRoute = new ConcurrentHashMap<String, GRIDnode>();
        routeSegments = new ConcurrentHashMap<String, GRIDrouteSegment>();

        // This is the class to change in order to use different weighting schemes
        theWeighter = new GRIDweightTime(ourMap);
        
        // This is where we change WHICH weighting scheme we are using. There has to be a better
        // way to change it other than hard coding
             
    }
    
    public void init() {
    	// Set things up here
    	//graph.loadEdges(ourMap);
    }

    public GRIDroute findPath(GRIDagent thisAgent, long currentTime) {
        GRIDfibHeap pq = new GRIDfibHeap();

        // Keep a map of all of the fibHeap entries along with the intersectionID associated with them
        Map<String, GRIDfibHeap.Entry> nodeList = new HashMap<>();
        GRIDnode startNodeValues;
        //ConcurrentMap<String, GRIDnodeWtTmEm> currentPathTotal = new ConcurrentHashMap<>();
        //ConcurrentHashMap<String, String> previousIntersections = new ConcurrentHashMap<>();
        /* BEGIN here is the new data structure for segments */
        //ConcurrentMap<String, GRIDrouteSegment> finalRouteSegments = new ConcurrentHashMap<>();
        /* END */
        
        //Long thisTimeslice = currentTime/1000;
        String agentFrom; 
        String agentTo;
        String agentID;

        // The agent is already on the link, so we need its endpoint
        agentID = thisAgent.getId();
        		     
        agentFrom = ourMap.getRoad(thisAgent.getCurrentLink()).getTo();
        
        /* The agent will end somewhere on the final link, so we need to get to its "from end"
         */

        // RCS Change to get from the directed graph
        //agtTo = graph.getRoad(thisAgent.getDestination()).getFrom();
        
        // agentTo is the Intersection at the start of the final road
        agentTo = ourMap.getRoad(thisAgent.getDestination()).getFrom();

        startNodeValues = new GRIDnode();
        startNodeValues.setNodeWeighttTotal(0.0);
        startNodeValues.setNodeTimeTotal(currentTime);
        GRIDnode tempNode = startNodeValues;

        /* source/destination check
         */
        //System.out.println("agtFrom: "+agtFrom);
        //System.out.println("agtTo: "+agtTo);

        /* DUMB check - prevent elsewhere
         */
        if (agentTo.equals(agentFrom)) {
        	
        	// RCS clean this up
        	logWriter.log(Level.WARNING, "Agent: " + thisAgent.getId() + " already at destination: " + agentTo);
        	return genDummyRoute("ARRIVED");
        }

        // RCS Add each node from the directedGraph to both the fibHeap and our list of entries
        
        // RCS do we need to enqueue (initialize) with thisTimeSlice, not 0L ? ? ? ? ?
        Iterator<String> iter = ourMap.getIntersectionIDs().iterator();
        while (iter.hasNext()) {
            String intersectionID = iter.next();
            
            // enqueue each Node (intersection) into the fibHeap, and then keep a reference to it in entries
        	nodeList.put(intersectionID, pq.enqueue(intersectionID, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0L));
        }
        
        // Set the current location to be the first selected by the queue
        pq.decreaseKey(nodeList.get(agentFrom), 0.0, 0.0, currentTime);

        // prime the while loop with the start node, which is the starting min
        GRIDfibHeap.Entry currFibEntry = pq.dequeueMin();
                
        double tempWeight;
        GRIDrouteSegment tempSegment = null; 
        String tempRoadID = "";
        
        while (!pq.isEmpty())
        {
        	// save the previous node
            workingRoute.put(currFibEntry.getValue(), tempNode);
            
            // create a new node
            tempNode = new GRIDnode();
           
            // step through every road leaving this intersection and 
            // update the priorities/weights of all of its edges.           
            for (Map.Entry<String, Double> arc : ourMap.reachableDestinations(currFibEntry.getValue()).entrySet()) {
                
            	// skip this road if we've already visited it
            	if (workingRoute.containsKey(arc.getKey())) continue;

            	long arrivalTime = currFibEntry.getTmTotal();
            	
                GRIDroad tempRoad = ourMap.hasRoad(currFibEntry.getValue(), arc.getKey());
                
                if (tempRoad.equals(null)) {
                	logWriter.log(Level.WARNING, "Unable to find road from: " + currFibEntry.getValue()+
                			                     " to: " + arc.getKey());
                	continue;
                }
               
            	logWriter.log(Level.INFO, "Checking the weight from: " + currFibEntry.getValue() + 
            			                  " to: "                      + arc.getKey() +
            			                  " at time: "                 + arrivalTime);
            	                                   	
            	// Get the weight from the current node to the proposed node
                tempWeight = theWeighter.calcWeight(currFibEntry.getValue(), 
                		                            this.ourMap.getIntersection(arc.getKey()).getId(),
                                                    arrivalTime);
                              
                System.out.println("tempNode is: " + tempNode.toString());
                System.out.println("time is:" + currFibEntry.getValue());

            	long traversalTime = tempRoad.getTravelTime(currFibEntry.getTmTotal());

                tempNode.setNodeWeighttTotal(tempWeight);
                
                /* If the weight of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */
                GRIDfibHeap.Entry dest = nodeList.get(arc.getKey());
                //Double newWeight = tempNode.getNodeWtTotal()+curr.getWtTotal();
                /* BEGIN new code for different weight calculations*/
                double newWeight = getEdgeWeight(tempNode)+currFibEntry.getWtTotal();
                /* END */
                
                logWriter.log(Level.INFO, "newWeight is: " + newWeight + " dest weight is: " + dest.getWtTotal() );
                		
                // If we can get to dest for less than the previous best, we want to use this route
                if (newWeight < dest.getWtTotal())
                {
                    Long tempTime = workingRoute.get(currFibEntry.getValue()).getNodeTimeTotal();

                    logWriter.log(Level.INFO, "tempTime is: " + tempTime);
                    
                    
                    //tempNode.setNodeTimeTotal(tempTime+tempNode.getNodeTimeTotal());
                    
                    //RCS remove
                    logWriter.log(Level.INFO, "setting node time to: " + tempNode.getNodeTimeTotal() );

                    Long tempTmTotal = tempNode.getNodeTimeTotal();
                    //Double tempEmissions = tempNode.getNodeEmissions();

                    pq.decreaseKey(dest, 0D, newWeight, tempTmTotal);
                   
                    // getRoad requires the road ID, not the combination of the start / end values
                    tempRoadID = ourMap.hasRoad(currFibEntry.getValue(), dest.getValue()).getId();
                    
                    if (tempRoadID.equals(null)) {
                    	// THIS IS BAD
                    	logWriter.log(Level.WARNING, "ATTEMPT TO USE NULL ROAD ID");
                    	continue;
                    }
                    
                    tempSegment = new GRIDrouteSegment();
                    
                    tempSegment.setRoad_ID(tempRoadID);
                    tempSegment.setStartIntersection(currFibEntry.getValue());
                    tempSegment.setEndIntersection(dest.getValue());
                    tempSegment.setTimeAtRoadEntry(tempTmTotal);
                    tempSegment.setTimeAtRoadExit(tempTime);
                
		            if(tempSegment != null) {
		            	// key these by their destination intersection, so we can build the route later
		            	routeSegments.put(tempSegment.getEndIntersection(), tempSegment);
		            	//routeSegmentsByStart.put(tempSegment.getStartIntersection(), tempSegment);
		            	
		            	//logWriter.log(Level.INFO, "Adding route segment: " + tempSegment.getRoad_ID());
		            }
		        }
            }
            /* Grab the current node.  The algorithm guarantees that we now
             * have the shortest distance to it.
             */
            currFibEntry = pq.dequeueMin();
            tempNode.setNodeTimeTotal(currFibEntry.getTmTotal());

            //System.out.println("grabbed: " + currFibEntry.getValue() + "\n");
            
            /* this conditional statement is necessary to correct for not starting
             * at the actual starting, i.e., from node for the starting link; we
             * can look at correcting this...
             */
            if(currFibEntry.getValue().equals(agentTo)) {
            }     
        }  // while (!pq.isEmpty())
                 
        ConcurrentHashMap<String, GRIDrouteSegment> routeSegmentsByStart = new ConcurrentHashMap<String, GRIDrouteSegment>();
//        ConcurrentHashMap<String, GRIDrouteSegment> routeSegmentsByEnd = new ConcurrentHashMap<String, GRIDrouteSegment>();
        // Now that we have all the segments, build the 2 lists so that we can find them easily
        
        for (Map.Entry<String, GRIDrouteSegment> theSegment : routeSegments.entrySet() ) {
        	
        	// RCS remove
        	//System.out.println("Adding segment: " + theSegment.getKey()+"\n");
        	
        	routeSegmentsByStart.put(theSegment.getValue().getStartIntersection(), theSegment.getValue());
//        	routeSegmentsByEnd.put(theSegment.getValue().getEndIntersection(), theSegment.getValue());
        }
                                                           
        GRIDroute finalRoute = new GRIDroute();
        finalRoute.setAgent_ID(agentID);
        
        // Start with the destination
        
        if(!routeSegments.containsKey(agentTo)){
            logWriter.log(Level.INFO, "Agent " + agentID + " is going to: " + agentTo + " - but that doesn't exist in the returned list");
            System.out.println("Agent " + agentID + " is going to: " + agentTo + " - but that doesn't exist in the returned list");
        	return genDummyRoute("Destination unreachable");
        }
        	
        tempSegment = (GRIDrouteSegment) routeSegments.get(agentTo);
        
        if( tempSegment == null) {
        	logWriter.log(Level.WARNING, "Destination intersection not found in route 1! from was: " + agentFrom + " dest was: " + agentTo);
        	return genDummyRoute("Destination unreachable");
        }
        
        // Put the final segment into the route
        finalRoute.pushSegment(tempSegment);
        
        boolean routeComplete = false;
        while(!routeComplete) {
        	// there should be only one segment in the collection that ends at the start point of the first segment of the route
        	String nextDest = tempSegment.getStartIntersection();
        	
        	tempSegment = (GRIDrouteSegment) routeSegments.get(nextDest);
        	
        	if( tempSegment == null) {
            	logWriter.log(Level.WARNING, "Destination intersection not found in route 1!");
            	return genDummyRoute("Destination unreachable");
            }
            
            // Put the final segment into the route
            finalRoute.pushSegment(tempSegment);
            
            if (tempSegment.getStartIntersection().equals(agentFrom)) {
            	// This is the end case
            	routeComplete = true;            	
            }
        }
          
        return finalRoute;
    }

    // Generate a dummy route. This is used when we fail computing a valid route, and want to not return null
    GRIDroute genDummyRoute(String reasonCode) {
    	GRIDroute dummyRoute = new GRIDroute();
    	dummyRoute.setAgent_ID(reasonCode);
    	return dummyRoute;
    }
    
    // Why do we need this?
    protected Double getEdgeWeight(GRIDnode thisNode) {
        return thisNode.getNodeWeightTotal();
    }

    // RCS we need to figure out how to do this with the routeSegments
    //while(previousIntersections.get(destName)!= null)
    //{
    //    destName = previousIntersections.get(destName);
    //    theIntersections.add(destName);
    //}

    

    /* BEGIN build segment list */
    
    // Iterate over the list BACKWARDS
    
    // RCS this won't be necessary if we fix the segments created above
//    String startIntersection = theIntersections.get(theIntersections.size()-1);     
//    GRIDrouteSegment tempSegment1;
    
//    ListIterator<String> iter = theIntersections.listIterator(theIntersections.size() -1);
//    while(iter.hasPrevious()) {
//    	String destIntersection = iter.previous();
//    	String tempRoadID = ourMap.hasRoad(startIntersection, destIntersection).getId();
    	
//    	tempSegment1 = new GRIDrouteSegment(startIntersection, destIntersection, tempRoadID);
	
//    	finalRoute.addSegment(tempSegment1);
//    	startIntersection = tempSegment1.getEndIntersection();
//    }
    
    
    
    // RCS FIX THIS TO USE THE GRIDDirectedGraph code
    //private static GRIDmap graphLoadEdges(GRIDmap myGraph) {
 //   private static GRIDDirectedGraph graphLoadEdges(GRIDmap myGraph) { 
  //  	Long startTime = System.nanoTime();

//        ArrayList<String> networkIntersections = new ArrayList<>(myGraph.getIntersections().keySet());
 //       ArrayList<GRIDroad> networkRoads = new ArrayList<>(myGraph.getRoads().values());

//        for(int i = 0; i < networkIntersections.size(); i++)
//        {
//            myGraph.addNode(networkIntersections.get(i));
//        }

//        for(int i = 0; i < networkRoads.size(); i++)
//        {
//            //if(i+1 == networkRoads.size()){System.out.println("edges: "+(i+1));}
//            myGraph.addEdge(networkRoads.get(i).getFrom(), networkRoads.get(i).getTo(), networkRoads.get(i).getLength());
//        }

//        long stopTime = System.nanoTime();
//        long timeToRun = ((stopTime - startTime)/1000000);

        //System.out.println(timeToRun/1000.0 + "s required for middleware\n");
//        return myGraph;
//    }

    
    //theIntersections.add(destName);
    
    // RCS This is where shit breaks. We "should" be able to get to the dest, but  .. .
//    if(previousIntersections.get(destName) == null)
//    {
//        System.out.println("\nI guess it's null, friend.");
        
//        System.out.println("Agent: " + agentID + " going from: " + agentFrom + " to: " + agentTo);
        
//        for(String intersection: previousIntersections.keySet()) {
//        	System.out.println("Intersection: " + intersection +" goes with " + previousIntersections.get(intersection));
//        }
                    
//        return genDummyRoute("Destination unreachable");
//    }

    // Build the route by finding the destination intersection in the hashmap of route segments
    
    
/* END GRIDpathfinder CLASS */
}

