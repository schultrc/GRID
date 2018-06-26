package edu.ucdenver.cse.GRIDserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDroad;

// the Directed Graph is used by the Fibonnacci Heap 
public class GRIDDirectedGraph implements Iterable<String> {

	private Map<String, Map<String, Double>> mGraph;

	public GRIDDirectedGraph() {
	
		// RCS what else needs to be created?
		mGraph = new HashMap<String, Map<String, Double>>();
	}
	
	
	//private ConcurrentMap<String, GRIDroad> Roads;
	//private ConcurrentMap<String, GRIDintersection> Intersections;
	
	// Move both to MAP ? ? ?
	
	//private ConcurrentMap<String, Long> intersectionList;
	//private ConcurrentMap<String, GRIDroad> roadList;
	
	
	//public Long getIntersectionListItem(String itemID) { return intersectionList.get(itemID); }
	//public void setIntersectionListItemTimeAtExit(String itemID, Long exitTime) {
		//GRIDintersection tempIntersection = new GRIDintersection();
	//}
	//@Override
	//public String toString() {
		//return "GRIDmap [Intersections=" + intersectionList + ", Roads=" + roadList + "]";
	//	return "GRIDmap [Intersections=" + intersectionList + "]";		
	//}

	//public GRIDroad getRoadListItem(String itemID) { return roadList.get(itemID); }

/*	public graphEdge getEdge(String start, String finish) // Map.Entry<String, Map.Entry<String, Double>>
	{
		graphEdge temp = new graphEdge();
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
*/
	
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
    	// RCS remove
		// Long startTime = System.nanoTime();

        ArrayList<String> networkIntersections = new ArrayList<>(myGraph.getIntersections().keySet());
        ArrayList<GRIDroad> networkRoads = new ArrayList<>(myGraph.getRoads().values());

        // replace with better for loop
        for(int i = 0; i < networkIntersections.size(); i++)
        {
            addNode(networkIntersections.get(i));
        }

        for(int i = 0; i < networkRoads.size(); i++)
        {
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
