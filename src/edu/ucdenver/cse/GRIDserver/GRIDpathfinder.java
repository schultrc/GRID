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
import java.util.logging.Level;

import java.util.concurrent.*;

import java.util.*;
import java.util.ListIterator;

public class GRIDpathfinder {

    private GRIDmap ourMap;
    //private GRIDDirectedGraph graph;
    
    private ConcurrentMap<String, GRIDnode> currentPathTotal;
    private ConcurrentHashMap<String, String> previousIntersections;
    //private ConcurrentHashMap<String, GRIDrouteSegment> routeSegmentsByStart;
    private ConcurrentHashMap<String, GRIDrouteSegment> routeSegments;

    private GRIDweight theWeighter;

    public GRIDpathfinder(GRIDmap theMap) {
    	this.ourMap = theMap;
    	
    	// RCS change to init the directed graph. MOVE TO INIT FUNCTION???
        // RCS I "Think" This is taken care of in the map
    	//graph = graphLoadEdges(theMap);
        
        currentPathTotal = new ConcurrentHashMap<String, GRIDnode>();
        previousIntersections = new ConcurrentHashMap<String, String>();
        //routeSegmentsByStart = new ConcurrentHashMap<String, GRIDrouteSegment>();
        routeSegments = new ConcurrentHashMap<String, GRIDrouteSegment>();

        
        //graph = new GRIDDirectedGraph();
        
        // This is the class to change in order to use different weighting schemes
        theWeighter = new GRIDweightTime(ourMap);
        
        // This is where we change WHICH weighting scheme we are using. There has to be a better
        // way to change it other than hard coding

        /* BEGIN pathfinder access quick-check*/
        //System.out.println("\nTEST TEST TEST\n");
        logWriter.log(Level.INFO, "\nTEST TEST TEST\n");
        /* END */

    }
    
    public void init() {
    	// Set things up here
        logWriter.log(Level.INFO, "is anything happening here...");
        //System.out.println("is anything happening here...");
    	ourMap.loadEdges(ourMap);
    }

    public GRIDroute findPath(GRIDagent thisAgent, Long currentTime) {
        GRIDfibHeap pq = new GRIDfibHeap();

        Map<String, GRIDfibHeap.Entry> entries = new HashMap<>();
        GRIDnode startNodeValues;
        //ConcurrentMap<String, GRIDnodeWtTmEm> currentPathTotal = new ConcurrentHashMap<>();
        //ConcurrentHashMap<String, String> previousIntersections = new ConcurrentHashMap<>();
        /* BEGIN here is the new data structure for segments */
        //ConcurrentMap<String, GRIDrouteSegment> finalRouteSegments = new ConcurrentHashMap<>();
        /* END */
        //Long thisTimeslice = currentTime/1000;
        Long thisTimeslice = currentTime;
        Long totalTravelTime = thisTimeslice;
        String agentFrom; 
        String agentTo;
        String agentID;

        /* BEGIN This is for testing only--open new console for debugging */
        //System.out.print("***** thisTimeslice: " + thisTimeslice + "*****");
        /* END */

        /* The agent is already on the link, so we need its endpoint
         */
        
        agentID = thisAgent.getId();

        agentFrom = ourMap.getRoad(thisAgent.getCurrentLink()).getTo();

        /* The agent will end somewhere on the final link, so we need to get to its "from end"
         */

        // RCS Change to get from the directed graph
        //agtTo = graph.getRoad(thisAgent.getDestination()).getFrom();
        
        // agentTo is the Intersection at the start of the final road
        agentTo = ourMap.getRoad(thisAgent.getDestination()).getFrom();

        startNodeValues = new GRIDnode();
        startNodeValues.initNodeValues(thisTimeslice, 0.0);
        /*startNodeValues.setNodeWeighttTotal(0.0);
        startNodeValues.setNodeTimeTotal(thisTimeslice);
        startNodeValues.setNodeEntryTime(thisTimeslice);*/
        GRIDnode tempNode = startNodeValues;

        /* source/destination check
         */
        //System.out.println("agtFrom: "+agtFrom);
        //System.out.println("agtTo: "+agtTo);

        /* DUMB check - prevent elsewhere
         */
        if (agentTo.equals(agentFrom)) {
        	
        	// RCS clean this up
            //logWriter.log(Level.WARNING, "Agent: " + thisAgent.getId() + " already at destination: " + agentTo);
            logWriter.log(Level.INFO, "Agent: " + thisAgent.getId() + " already at destination: " + agentTo);
        	return genDummyRoute("ARRIVED");
        }

        /* roadList creation--necessary for fibHeap mGraph data structure
         */
        // RCS is this already done in the map? Do we need to do it in the directed graph instead?
        //graph.initMap();;
        //ourMap.initMap();

        // Replace the iterable part of GRIDmap
        
        for (String node : ourMap)
            entries.put(node, pq.enqueue(node, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, currentTime));

        pq.decreaseKey(entries.get(agentFrom), 0.0, 0.0, thisTimeslice);

        /* prime the while loop with the start node, which is the starting min
         */
        GRIDfibHeap.Entry curr = pq.dequeueMin();

        double tempWeight;
        GRIDrouteSegment tempSegment = null; 
        String tempRoadID = "";
        
        while (!pq.isEmpty())
        {
            currentPathTotal.put(curr.getValue(), tempNode);

            // Update the priorities/weights of all of its edges.
           
            // step through every road leaving this intersection
            for (Map.Entry<String, Double> arc : ourMap.edgesFrom(curr.getValue()).entrySet()) {

            	// skip this road if we've already visited it
            	if (currentPathTotal.containsKey(arc.getKey())) continue;

                /* BEGIN testing for precog values */
                //logWriter.log(Level.INFO, "Road: " + curr.getValue()+"to"+ arc.getKey());
                //System.out.println("Road: " + curr.getValue()+"to"+ arc.getKey());
                //String myTemp = curr.getValue()+"to"+ arc.getKey();
                //logWriter.log(Level.INFO, "Values: "+ourMap.getRoad(myTemp).getVehiclesCurrentlyOnRoadAtTime());
                //System.out.println("Values: "+ourMap.getRoad(myTemp).getVehiclesCurrentlyOnRoadAtTime());
                /* END */

                /* Compute the cost of the path from the source to this node,
                 * which is the cost of this node plus the cost of this edge.
                 */
                
                // RCS incorporate the new helper classes here
                // RCS this will need the map, not the graph
               
                tempWeight = theWeighter.calcWeight(curr.getValue(), 
                		                            arc.getKey(),
                                                    currentPathTotal.get(curr.getValue()).getNodeTimeTotal());

                /* BEGIN code to check currentPathTotal values */
                //logWriter.log(Level.INFO, "Values: " + currentPathTotal.get(curr.getValue()).getNodeTimeTotal());
                /* END */
               
                tempNode.setNodeWeighttTotal(tempWeight);
                // MFS including newTime here...replace with appropriate method later...
                tempNode.setNodeTimeTotal(tempNode.getNodeTimeTotal()+(tempNode.getNodeTimeAtEntry()));
                
                /* If the length of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */
                GRIDfibHeap.Entry dest = entries.get(arc.getKey());
                //Double newWeight = tempNode.getNodeWtTotal()+curr.getWtTotal();
                /* BEGIN new code for different weight calculations*/
                double newWeight = getEdgeWeight(tempNode)+curr.getWtTotal();
                //logWriter.log(Level.INFO, "newWeight is: " + newWeight + " dest weight is: " + dest.getWtTotal() + "\n");
                //System.out.println("newWeight is: " + newWeight + " dest weight is: " + dest.getWtTotal() + "\n");
                /* END */

                if (newWeight < dest.getWtTotal())
                {
                    Long tempTime = currentPathTotal.get(curr.getValue()).getNodeTimeTotal();

                    //tempNode.setNodeTimeTotal(tempTime+tempNode.getNodeTimeTotal());//

                    // MFS set the new entry time

                    tempNode.setNodeTimeTotal(tempTime);

                    Long tempTmTotal = tempNode.getNodeTimeTotal();
                    //Double tempEmissions = tempNode.getNodeEmissions();

                    pq.decreaseKey(dest, 0D, newWeight, tempTmTotal);
                   
                    // RCS I think we can remove this . . .
                    //previousIntersections.put(dest.getValue(),curr.getValue());

                    //logWriter.log(Level.INFO, "We used to add: " + dest.getValue() + " and " + curr.getValue() + "\n");
                    //System.out.println("We used to add: " + dest.getValue() + " and " + curr.getValue() + "\n");
                    /* BEGIN here is the new data structure for segments */
                    // RCS fix to get road ID
                    //String tempString = graph.getRoadListItem(curr.getValue()+dest.getValue()).getId();
                    
                    //RCS changed 
                    //String tempString = graph.getRoad(curr.getValue()+dest.getValue()).getId();
                    
                    // getRoad requires the road ID, not the combination of the start / end values
                    tempRoadID = ourMap.hasRoad(curr.getValue(), dest.getValue()).getId();

                    //if (tempRoadID.equals(null)) {
                    if(tempRoadID == null) {
                    	// THIS IS BAD
                        System.out.println("WARNING: attempting to use null road ID...");
                        logWriter.log(Level.WARNING, "ATTEMPT TO USE NULL ROAD ID");
                    	continue;
                    }

                    tempSegment = new GRIDrouteSegment();

                    tempSegment.setRoad_ID(tempRoadID);
                    tempSegment.setStartIntersection(curr.getValue());
                    tempSegment.setEndIntersection(dest.getValue());

                    //MFS make addition to attempt to handle time at road entry here?
                    tempSegment.setTimeAtRoadExit(tempTime);

                    //MFS sending to log/console the time on road--maybe...
                    //logWriter.log(Level.INFO, "in:  "+tempNode.getNodeTimeAtEntry());
                    //System.out.println("in:  "+tempNode.getNodeTimeAtEntry());
                    //logWriter.log(Level.INFO, "out: "+tempSegment.getTimeAtRoadExit());
                    //System.out.println("out: "+tempSegment.getTimeAtRoadExit());
                    //logWriter.log(Level.INFO, "time on road: "+(tempSegment.getTimeAtRoadExit()-tempNode.getNodeTimeAtEntry()));
                    //System.out.println("time on road: "+(tempSegment.getTimeAtRoadExit()-tempNode.getNodeTimeAtEntry()));

                    //logWriter.log(Level.INFO, "added new segment for road: " + tempRoadID);

                   // finalRouteSegments.put(tempString, tempSegment);

                    //GRIDrouteSegment tempSegment = new GRIDrouteSegment(tempString, tempTmTotal, tempEmissions);
                    /* END */

		            if(tempSegment != null) {
		            	// key these by their destination intersection, so we can build the route later
		            	routeSegments.put(tempSegment.getEndIntersection(), tempSegment);
		            	//routeSegmentsByStart.put(tempSegment.getStartIntersection(), tempSegment);

		            	logWriter.log(Level.INFO, "Adding route segment: " + tempSegment.getRoad_ID());
		            }
                }
            }
            /* Grab the current node.  The algorithm guarantees that we now
             * have the shortest distance to it.
             */
            curr = pq.dequeueMin();
            tempNode.setNodeTimeTotal(curr.getTmTotal());

            // MFS log/console output for testing
            //logWriter.log(Level.INFO, "grabbed: " + curr.getValue() + "\n");
            //System.out.println("grabbed: " + curr.getValue() + "\n");
            
            /* this conditional statement is necessary to correct for not starting
             * at the actual starting, i.e., from node for the starting link; we
             * can look at correcting this...
             */
            if(curr.getValue().equals(agentTo)) {
            	totalTravelTime = curr.getTmTotal();
            }     
        }
         
        String destName = agentTo;

        
        ConcurrentHashMap<String, GRIDrouteSegment> routeSegmentsByStart = new ConcurrentHashMap<String, GRIDrouteSegment>();
//        ConcurrentHashMap<String, GRIDrouteSegment> routeSegmentsByEnd = new ConcurrentHashMap<String, GRIDrouteSegment>();
        // Now that we have all the segments, build the 2 lists so that we can find them easily
        
        for (Map.Entry<String, GRIDrouteSegment> theSegment : routeSegments.entrySet() ) {

            // MFS adding logging here for adding segment
            //logWriter.log(Level.INFO, "Adding segment: " + theSegment.getKey()+" ("+theSegment.getValue()+")\n");
        	//System.out.println("Adding segment: " + theSegment.getKey()+" ("+theSegment.getValue()+")\n");
        	
        	routeSegmentsByStart.put(theSegment.getValue().getStartIntersection(), theSegment.getValue());
//        	routeSegmentsByEnd.put(theSegment.getValue().getEndIntersection(), theSegment.getValue());
        }
                                                      
      
        GRIDroute finalRoute = new GRIDroute();
        finalRoute.setAgent_ID(agentID);
        
        // Start with the destination
        
        if(!routeSegments.containsKey(destName)){
            logWriter.log(Level.INFO, "Agent " + agentID + " is going to: " + agentTo + " - but that doesn't exist in the returned list");
            //System.out.println("Agent " + agentID + " is going to: " + agentTo + " - but that doesn't exist in the returned list");
        	return genDummyRoute("Destination unreachable");
        }
        	
        tempSegment = (GRIDrouteSegment) routeSegments.get(destName);
        
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

