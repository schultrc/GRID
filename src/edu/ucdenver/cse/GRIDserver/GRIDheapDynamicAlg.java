/*
    Code Citation:

    Original designer: Keith Schwarz, Stanford CS Dept.

    Using code from the following website as the basis for the Fibonacci heap
    data structure for our Dijkstra's implementation:

    http://www.keithschwarz.com/interesting/
    http://www.keithschwarz.com/interesting/code/?dir=fibonacci-heap
*/

package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDnodeWtTmEm;
import edu.ucdenver.cse.GRIDmap.GRIDroad;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.logWriter;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.*;

public class GRIDheapDynamicAlg {
    private GRIDmap graph;
    GRIDroute finalPath;
    
    public GRIDheapDynamicAlg(GRIDmap thisMap) {
        //graph = thisMap;
        this.graph = graphMiddleware(thisMap);
        this.finalPath = new GRIDroute();
    }

    public GRIDroute findPath(GRIDagent thisAgent, Long currentTime) {
        GRIDfibHeap pq = new GRIDfibHeap();

        Map<String, GRIDfibHeap.Entry> entries = new HashMap<>();
        GRIDnodeWtTmEm startNodeValues;
        ConcurrentMap<String, GRIDnodeWtTmEm> currentPathTotal = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> previousIntersections = new ConcurrentHashMap<>();

        Long thisTimeslice = currentTime;

        Long totalTravelTime = thisTimeslice;
        String agtFrom, agtTo;

        /* The agent is already on the link, so we need its end point
         */
        agtFrom = graph.getRoad(thisAgent.getCurrentLink()).getTo();
        /* The agent will end somewhere on the final link, so we need to get to its "from end"
         */
        agtTo = graph.getRoad(thisAgent.getDestination()).getFrom();

        if (agtTo.equals(agtFrom)) {
            return errorRoute();
        }
        
        // Initialize the starting values
        startNodeValues = new GRIDnodeWtTmEm();
        startNodeValues.setNodeWtTotal(0.0);
        startNodeValues.setNodeTmTotal(thisTimeslice);
        GRIDnodeWtTmEm tempNode = startNodeValues;

        /* source/destination check
         */
        //System.out.println("agtFrom: "+agtFrom);
        //System.out.println("agtTo: "+agtTo);

        

        /* roadList creation--this will be fixed in the GRIDmap class by
         * combining operations of GRIDmap and the DirectedGraph class
         * more seamlessly
         */
        graph.initMap();

        for (String node : graph)
            entries.put(node, pq.enqueue(node, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0L));

        pq.decreaseKey(entries.get(agtFrom), 0.0, 0.0, thisTimeslice);

        /* prime the while loop with the start node, which is the starting min
         */
        GRIDfibHeap.Entry curr = pq.dequeueMin();

        while (!pq.isEmpty())
        {
            currentPathTotal.put(curr.getValue(), tempNode);

            /* Update the priorities/weights of all of its edges.
             */
            for (Map.Entry<String, Double> arc : graph.edgesFrom(curr.getValue()).entrySet()) {
                if (currentPathTotal.containsKey(arc.getKey())) continue;

                /* Compute the cost of the path from the source to this node,
                 * which is the cost of this node plus the cost of this edge.
                 */
                
              //  logWriter.log(Level.INFO, "GRIDheapDynamicAlg: findPath - " +
              //  		                  " prior to calcWeight, time is: " + currentPathTotal.get(curr.getValue()).getNodeTmTotal());
                
                
                // RCS Integrate external classes here
                
                tempNode = graph.calcWeight(curr.getValue(), arc.getKey(),
                        currentPathTotal.get(curr.getValue()).getNodeTmTotal());

                /* If the length of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */
                GRIDfibHeap.Entry dest = entries.get(arc.getKey());

                if ((tempNode.getNodeWtTotal()+curr.getWtTotal()) < dest.getWtTotal())
                {
                    Long tempTime = currentPathTotal.get(curr.getValue()).getNodeTmTotal();

                    tempNode.setNodeTmTotal(tempTime+tempNode.getNodeTmTotal());
                    Long tempTmTotal = tempNode.getNodeTmTotal();

                    pq.decreaseKey(dest, 0D, (tempNode.getNodeWtTotal()+curr.getWtTotal()),tempTmTotal);
                    previousIntersections.put(dest.getValue(),curr.getValue());
                }
            }

            /* Grab the current node.  The algorithm guarantees that we now
             * have the shortest distance to it.
             */
            curr = pq.dequeueMin();
            tempNode.setNodeTmTotal(curr.getTmTotal());

            /* this conditional statement is necessary to correct for not starting
             * at the actual starting, i.e., from node for the starting link; we
             * can look at correcting this...
             */
            if(curr.getValue().equals(agtTo)) totalTravelTime = curr.getTmTotal();
        }

        String step = agtTo;

        finalPath.getIntersections().add(step);
        if(previousIntersections.get(step) == null)
        {
        	// RCS log this
            logWriter.log(Level.WARNING, "I guess it's null, friend.");
            return errorRoute();
        }

        /* Create the final path from source to destination
         */
        while(previousIntersections.get(step)!= null)
        {
            step = previousIntersections.get(step);
            finalPath.getIntersections().add(step);
        }

        // RCS make an internal call that does this?
        // Collections.reverse(finalPath.getIntersections());

        // RCS make an internal call that calculates this?
        //finalPath.setCalculatedTravelTime(totalTravelTime);
        
        return finalPath;
    }

    private GRIDroute errorRoute() {
    	// Should we do something with this to make it clear that it is null?
    	return finalPath;
    }
    private GRIDmap graphMiddleware(GRIDmap myGraph) {
        Long startTime = System.nanoTime();
        
        ArrayList<String> networkIntersections = new ArrayList<>(myGraph.getIntersections().keySet());
        ArrayList<GRIDroad> networkRoads = new ArrayList<>(myGraph.getRoads().values());

        for(int i = 0; i < networkIntersections.size(); i++)
        {
            //if(i+1 == networkIntersections.size()){System.out.println("nodes: "+(i+1));}
            myGraph.addNode(networkIntersections.get(i));
        }

        for(int i = 0; i < networkRoads.size(); i++)
        {
            //if(i+1 == networkRoads.size()){System.out.println("edges: "+(i+1));}
        	
        	//logWriter.log(Level.INFO, "Adding road: " + );
            myGraph.addEdge(networkRoads.get(i).getFrom(), networkRoads.get(i).getTo(), networkRoads.get(i).getLength());
        }

        long stopTime = System.nanoTime();
        long timeToRun = ((stopTime - startTime)/1000000);

        //System.out.println(timeToRun/1000.0 + "s required for middleware\n");
        return myGraph;
    }

/* END GRIDheapDynamicAlg CLASS */
}

