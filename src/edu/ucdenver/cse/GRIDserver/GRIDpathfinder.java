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
import edu.ucdenver.cse.GRIDmap.*;

import java.util.concurrent.*;
import java.util.*;

public class GRIDpathfinder {
    private static GRIDmap graph;
    private ConcurrentMap<String, GRIDnodeWtTmEm> currentPathTotal = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> previousIntersections = new ConcurrentHashMap<>();
    private ConcurrentMap<String, GRIDrouteSegment> finalRouteSegments = new ConcurrentHashMap<>();

    public GRIDpathfinder(GRIDmap thisMap) {
        graph = graphLoadEdges(thisMap);
    }

    public static class GRIDgreenPathfinder extends GRIDpathfinder {

        public GRIDgreenPathfinder(GRIDmap thisMap) {
            super(thisMap);
        };

        public Double getEdgeWeight(GRIDnodeWtTmEm thisNode) {
            return thisNode.getNodeEmissions()+thisNode.getNodeWtTotal();
        }
    }

    public GRIDroute findPath(GRIDagent thisAgent, Long currentTime) {
        GRIDfibHeap pq = new GRIDfibHeap();

        Map<String, GRIDfibHeap.Entry> entries = new HashMap<>();
        GRIDnodeWtTmEm startNodeValues;
        //ConcurrentMap<String, GRIDnodeWtTmEm> currentPathTotal = new ConcurrentHashMap<>();
        //ConcurrentHashMap<String, String> previousIntersections = new ConcurrentHashMap<>();
        /* BEGIN here is the new data structure for segments */
        //ConcurrentMap<String, GRIDrouteSegment> finalRouteSegments = new ConcurrentHashMap<>();
        /* END */
        Long thisTimeslice = currentTime/1000;
        Long totalTravelTime = thisTimeslice;
        String agtFrom, agtTo;

        /* The agent is already on the link, so we need its endpoint
         */
        agtFrom = graph.getRoad(thisAgent.getCurrentLink()).getTo();
        /* The agent will end somewhere on the final link, so we need to get to its "from end"
         */
        agtTo = graph.getRoad(thisAgent.getDestination()).getFrom();

        startNodeValues = new GRIDnodeWtTmEm();
        startNodeValues.setNodeWtTotal(0.0);
        startNodeValues.setNodeTmTotal(thisTimeslice);
        GRIDnodeWtTmEm tempNode = startNodeValues;

        /* source/destination check
         */
        //System.out.println("agtFrom: "+agtFrom);
        //System.out.println("agtTo: "+agtTo);

        /* DUMB check - prevent elsewhere
         */
        if (agtTo.equals(agtFrom)) {
            return null;
        }

        /* roadList creation--necessary for fibHeap mGraph data structure
         */
        graph.loadRoadList();

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
                tempNode = graph.calcWeight(curr.getValue(), arc.getKey(),
                        currentPathTotal.get(curr.getValue()).getNodeTmTotal());

                /* If the length of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */
                GRIDfibHeap.Entry dest = entries.get(arc.getKey());
                //Double newWeight = tempNode.getNodeWtTotal()+curr.getWtTotal();
                /* BEGIN new code for different weight calculations*/
                Double newWeight = getEdgeWeight(tempNode)+curr.getWtTotal();
                /* END */

                if (newWeight < dest.getWtTotal())
                {
                    Long tempTime = currentPathTotal.get(curr.getValue()).getNodeTmTotal();

                    tempNode.setNodeTmTotal(tempTime+tempNode.getNodeTmTotal());
                    Long tempTmTotal = tempNode.getNodeTmTotal();
                    Double tempEmissions = tempNode.getNodeEmissions();

                    pq.decreaseKey(dest, 0D, newWeight, tempTmTotal);
                    previousIntersections.put(dest.getValue(),curr.getValue());

                    /* BEGIN here is the new data structure for segments */
                    String tempString = graph.getRoadListItem(curr.getValue()+dest.getValue()).getId();
                    GRIDrouteSegment tempSegment = new GRIDrouteSegment(tempString, tempTmTotal, tempEmissions);
                    finalRouteSegments.put(tempString, tempSegment);
                    /* END */
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

        /* BEGIN weight/time testing
         * 38347489_0 (404)
         * 38347521_0 (404)
         * 1779115801223351743
        */
        //GRIDroad tempTestRoad = graph.getRoadListItem("1779115801223351743");
        //System.out.println("weight on 38347521_0 at 404: "+tempTestRoad.getWeightAtTime(404L));
        /* END weight/time testing */

        GRIDroute finalPath = new GRIDroute();

        String step = agtTo;

        finalPath.getIntersections().add(step);
        if(previousIntersections.get(step) == null)
        {
            System.out.println("\nI guess it's null, friend.");
            return null;
        }

        /* Create the final path from source to destination
         */
        while(previousIntersections.get(step)!= null)
        {
            step = previousIntersections.get(step);
            finalPath.getIntersections().add(step);
        }

        Collections.reverse(finalPath.getIntersections());

        /* BEGIN build segment list */
        finalPath.setRouteSegments(graph.getPathBySegment(finalPath.getIntersections(), finalRouteSegments));
        /* END */

        finalPath.setCalculatedTravelTime(totalTravelTime);
        finalPath.setCalculatedEmissionsTotal();
        return finalPath;
    }

    protected Double getEdgeWeight(GRIDnodeWtTmEm thisNode) {
        return thisNode.getNodeWtTotal();
    }

    private static GRIDmap graphLoadEdges(GRIDmap myGraph) {
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
            myGraph.addEdge(networkRoads.get(i).getFrom(), networkRoads.get(i).getTo(), networkRoads.get(i).getLength());
        }

        long stopTime = System.nanoTime();
        long timeToRun = ((stopTime - startTime)/1000000);

        //System.out.println(timeToRun/1000.0 + "s required for middleware\n");
        return myGraph;
    }

/* END GRIDpathfinder CLASS */
}

