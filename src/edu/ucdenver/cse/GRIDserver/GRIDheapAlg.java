/*
    Using code from the following website as example to follow in creation
    of our Dijkstra's implementation:
    Keith Schwartz
*/

package edu.ucdenver.cse.GRIDserver;

import java.util.concurrent.*;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import java.util.*;

public class GRIDheapAlg {

    public ArrayList<String> shortestPath(GRIDmap graph, String source, String destination) {
        /* Create a Fibonacci heap storing the distances of unvisited nodes
         * from the source node.
         */
        GRIDfibHeap pq = new GRIDfibHeap();

        /* The Fibonacci heap uses an internal representation that hands back
         * Entry objects for every stored element.  This map associates each
         * node in the graph with its corresponding Entry.
         */
        Map<String, GRIDfibHeap.Entry> entries = new HashMap<String, GRIDfibHeap.Entry>();

        /* Maintain a map from nodes to their distances.  Whenever we expand a
         * node for the first time, we'll put it in here.
         */
        Map<String, Double> result = new HashMap<String, Double>();
        ConcurrentMap<String, String> previousIntersections = new ConcurrentHashMap<>();

        /* Add each node to the Fibonacci heap at distance +infinity since
         * initially all nodes are unreachable.
         */
        for (String node : graph)
            entries.put(node, pq.enqueue(node, Double.POSITIVE_INFINITY, 0D, 0L));

        /* Update the source so that it's at distance 0.0 from itself; after
         * all, we can get there with a path of length zero!
         */
        pq.decreaseKey(entries.get(source), 0.0, 0.0, 0L);

        /* Grab the current node.  The algorithm guarantees that we now
             * have the shortest distance to it.
             */
        GRIDfibHeap.Entry curr = pq.dequeueMin();
        System.out.println("Source: " + curr.getValue());

        /* Keep processing the queue until no nodes remain. */
        while (!pq.isEmpty())
        {
            //System.out.println("\ncurrent entry: "+curr.getValue());

            /* Store this in the result table. */
            result.put(curr.getValue(), curr.getPriority());

            /* Update the priorities of all of its edges. result.entrySet()
            * graph.edgesFrom(curr.getValue().entrySet()) */
            for (Map.Entry<String, Double> arc : graph.edgesFrom(curr.getValue()).entrySet()) {
                /* If we already know the shortest path from the source to
                 * this node, don't add the edge.
                 */
                if (result.containsKey(arc.getKey())) continue;

                /* Compute the cost of the path from the source to this node,
                 * which is the cost of this node plus the cost of this edge.
                 */
                double pathCost = curr.getPriority() + arc.getValue();
                GRIDmap.graphEdge test000 = graph.getEdge(curr.getValue(), arc.getKey());

                //System.out.println(test000+" pathCost: "+pathCost);

                /* If the length of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */
                GRIDfibHeap.Entry dest = entries.get(arc.getKey());
                if (pathCost < dest.getPriority())
                {
                    pq.decreaseKey(dest, pathCost, 0.0, 0L);
                    previousIntersections.put(dest.getValue(),curr.getValue());
                }
            }

            /* Grab the current node.  The algorithm guarantees that we now
             * have the shortest distance to it.
             */
            curr = pq.dequeueMin();

            if(curr.getValue() == destination) {
                System.out.println("This should be the destination: " + curr.getValue());
                pq.clearFibHeap();
            }
        }

        ArrayList<String> finalPath = new ArrayList<String>();
        String step = destination;

        finalPath.add(step);
        if(previousIntersections.get(step) == null)
        {
            System.out.println("\nI guess it's null, friend.");
            return null;
        }

        /* Create the final path from source to destination */
        while(previousIntersections.get(step)!= null)
        {
            step = previousIntersections.get(step);
            finalPath.add(step);
        }

        Collections.reverse(finalPath);

        /* Finally, report the distances we've found.
        for(Map.Entry<String, String> stepNode : previousIntersections.entrySet())
        {
            System.out.print("step: " +stepNode+" ");
        }
        System.out.print("\n"); */

        Map<String,String> orderedPath = new HashMap<>();
        Set keys = previousIntersections.keySet();

        /* * * * * * * * * * * * * * * * * * * * * * * * * * *
         * START OUTPUT FOR TESTING
         * This for loop returns the keys and values for the
         * previousIntersections ConcurrentMap (unsorted)
         * * * * * * * * * * * * * * * * * * * * * * * * * * *
        for (Iterator itr = keys.iterator(); itr.hasNext();)
        {
            String key = (String) itr.next();
            String value = (String) previousIntersections.get(key);
            String newStart = value;
            System.out.print("K: "+key+" V: "+value+" ");
            orderedPath.put(key,value);

        }

        System.out.println("\nThis List: "+orderedPath);
        System.out.println("\nFinal Path: "+finalPath);
        * END OUTPUT FOR TESTING */

        return finalPath;
    }

}

