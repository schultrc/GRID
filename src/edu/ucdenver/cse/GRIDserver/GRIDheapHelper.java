package edu.ucdenver.cse.GRIDserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.ucdenver.cse.GRIDmap.GRIDnode;
import edu.ucdenver.cse.GRIDmap.GRIDroad;
import edu.ucdenver.cse.GRIDmap.GRIDintersection;

public class GRIDheapHelper {

	// These 2 may need to come from the map
	private ConcurrentMap<String, GRIDroad> Roads;
	private ConcurrentMap<String, GRIDintersection> Intersections;
	
	// ?
	private ConcurrentMap<String, Long> intersectionList;

	
	private ConcurrentMap<String, GRIDroad> roadList;

	
	public GRIDnode calcWeight(String startNode, String endNode, long startTime)
    {
        double tempWeight = 0.0;
        long tempTimeslice = 0L;
        GRIDnode tempNode = new GRIDnode();
		GRIDroad tempRoad = this.getRoadListItem(startNode+endNode);

        tempTimeslice = tempRoad.getTravelTime();
        
        
        //tempWeight = tempRoad.getTimeWeightOverInterval(startTime);

        // USE THE NEW WEIGHT OBJECT TO CALC THE WEIGHT!!!!!
        
        tempNode.setNodeWeighttTotal(tempWeight);
        tempNode.setNodeTimeTotal(tempTimeslice);

        return tempNode;
    }
	
	public void initMap() {
		loadRoadList();
	}
	
	// This is unneeded and can be replaced / overcome with a proper search method on roads
	public void loadRoadList() {
		this.Roads.forEach((item,value)->
                roadList.put(value.getFrom()+value.getTo(),value));
		this.Intersections.forEach((item,value)->
				intersectionList.put(value.getId(),0L));
    }
	
	public Long getIntersectionListItem(String itemID) { return intersectionList.get(itemID); }
	public void setIntersectionListItemTimeAtExit(String itemID, Long exitTime) {
		//GRIDintersection tempIntersection = new GRIDintersection();
	}
	@Override
	public String toString() {
		return "GRIDmap [Intersections=" + Intersections + ", Roads=" + Roads + "]";
	}

	public GRIDroad getRoadListItem(String itemID) { return roadList.get(itemID); }

	
	
	
	
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	//************************************************************************************
	
	/*  *
	* The following functions are transplanted from the DirectedGraph class.
	* These need to be reviewed and cleaned up for better integration, i.e.,
	* eliminating the need for the 'middleware' function and using only roads
	* rather than pulling from the edge list as created by the original
	* DirectedGraph class.
	* * */

	private final Map<String, Map<String, Double>> mGraph = new HashMap<String, Map<String, Double>>();
	private final ConcurrentMap<String, Map<String, Double>> fibHeapGraph = new ConcurrentHashMap<String, Map<String, Double>>();
	
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

	public graphEdge getEdge(String start, String finish) // Map.Entry<String, Map.Entry<String, Double>>
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

	
	/**
	 * Adds a new node to the graph.  If the node already exists, this
	 * function is a no-op.
	 *
	 * @param node The node to add.
	 * @return Whether or not the node was added.
	 */
	public boolean addNode(String node) {
        /* If the node already exists, don't do anything. */
		if (fibHeapGraph.containsKey(node))
			return false;

        /* Otherwise, add the node with an empty set of outgoing edges. */
		fibHeapGraph.put(node, new HashMap<String, Double>());
		return true;
	}

	/**
	 * Given a start node, destination, and length, adds an arc from the
	 * start node to the destination of the length.  If an arc already
	 * existed, the length is updated to the specified value.  If either
	 * endpoint does not exist in the graph, throws a NoSuchElementException.
	 *
	 * @param start The start node.
	 * @param dest The destination node.
	 * @param length The length of the edge.
	 * @throws NoSuchElementException If either the start or destination nodes
	 *                                do not exist.
	 */
	public void addEdge(String start, String dest, double length) {
        /* Confirm both endpoints exist. */
		if (!fibHeapGraph.containsKey(start) || !fibHeapGraph.containsKey(dest))
			throw new NoSuchElementException("Both nodes must be in the graph.");

        /* Add the edge. */
		fibHeapGraph.get(start).put(dest, length);
	}

	/**
	 * Given a node in the graph, returns an immutable view of the edges
	 * leaving that node, as a map from endpoints to costs.
	 *
	 * @param node The node whose edges should be queried.
	 * @return An immutable view of the edges leaving that node.
	 * @throws NoSuchElementException If the node does not exist.
	 */
    public Map<String, Double> edgesFrom(String node) {
        /* Check that the node exists. */
		Map<String, Double> arcs = fibHeapGraph.get(node);
		if (arcs == null)
			throw new NoSuchElementException("Source node does not exist.");

		return Collections.unmodifiableMap(arcs);
	}

	/**
	 * Returns an iterator that can traverse the nodes in the graph.
	 *
	 * @return An iterator that traverses the nodes in the graph.
	 */
	public Iterator<String> iterator() {
		return fibHeapGraph.keySet().iterator();
	}
}
