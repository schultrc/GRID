/*
    Using code from the following website as example to follow in creation
    of our Dijkstra's implementation:
    http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
*/

package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDroad;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;

import java.util.concurrent.*;
import java.util.*;

public class GRIDselfishAlg {

    private int testCounter;
    private ConcurrentMap<String, GRIDroad> roads;
    private String agtFrom;
    private String agtTo;
    private Long currentTime;
    private Set<String> visited;
    private Set<String> unVisited;
    private ConcurrentMap<String, Double> currentPathTotal;
    private Map<String,String> previousIntersections;

    public GRIDselfishAlg(GRIDagent thisAgent, GRIDmap selfishMap, Long currentTime){

        testCounter = 0;
        this.roads = selfishMap.getRoads();
        agtFrom = thisAgent.getOrigin();
        agtTo = thisAgent.getDestination();
        this.currentTime = currentTime;
    }

    public GRIDroute findPath(){
        visited = new HashSet<String>();
        unVisited = new HashSet<String>();
        currentPathTotal = new ConcurrentHashMap<>();
        previousIntersections = new HashMap<String,String>();
        currentPathTotal.put(agtFrom,0D);
        unVisited.add(agtFrom);

        while(unVisited.size() > 0){
            String node = getMin(unVisited);
            visited.add(node);
            unVisited.remove(node);
            findOptimalEdges(node);
        }

        GRIDroute finalPath = new GRIDroute();
        String step = agtTo;

        finalPath.getIntersections().add(step);
        if(previousIntersections.get(step) == null)
        {
            System.out.println("\nI guess it's null, friend.");
            return null;
        }

        while(previousIntersections.get(step)!= null)
        {
            //System.out.println("step before: " + step);
            step = previousIntersections.get(step);
            finalPath.getIntersections().add(step);
            //System.out.println("step after:  " + step);
        }

        Collections.reverse(finalPath.getIntersections());

        System.out.println("path total: "+currentPathTotal);
        System.out.println("Returning path. . .");
        return finalPath;
    }

    private String getMin(Set<String> nodes){
        String min = null;

        for(String node : nodes){
            if(min == null)
            {
                min = node;
            }
            else
            {
                if(getOptimalEdgeWeight(node) < getOptimalEdgeWeight(min))
                {
                    min = node;
                }
            }
        }

        return min;
    }

    private void findOptimalEdges(String startNode)
    {
        ArrayList<String> adjNodes = getAdjNodes(startNode);

        for(String endNode : adjNodes)
        {
            if(getOptimalEdgeWeight(endNode) > getOptimalEdgeWeight(startNode) + calcEdgeWeight(startNode, endNode))
            {
                currentPathTotal.put(endNode,getOptimalEdgeWeight(startNode) + calcEdgeWeight(startNode, endNode));
                //System.out.println("Previous nodes: " + previousIntersections);

                previousIntersections.put(endNode,startNode);
                unVisited.add(endNode);
            }
        }
    }

    private Double getOptimalEdgeWeight(String endNode)
    {
        Double w = currentPathTotal.get(endNode);

        if(w == null)
        {
            return Double.MAX_VALUE;
        }
        else{
            return w;
        }
    }

    private Double calcEdgeWeight(String startNode, String endNode)
    {
        for(String roadId : roads.keySet())
        {
            //roads.get(roadId).getFrom()
            if(roads.get(roadId).getFrom().equals(startNode)
               && roads.get(roadId).getTo().equals(endNode))
            {
                //return roads.get(roadId).getWeightAtTime(currentTime);
                return roads.get(roadId).getLength();
            }
        }

        return -1D;
    }

    private ArrayList<String> getAdjNodes(String node)
    {
        ArrayList<String> adjNodes = new ArrayList<String>();

        for(String key : roads.keySet())
        {
            if(roads.get(key).getFrom().equals(node) && !isVisited(roads.get(key).getTo()))
            {
                adjNodes.add(roads.get(key).getTo());
            }
        }

        return adjNodes;
    }

    private boolean isVisited(String node)
    {
        for (String intrx : visited)
        {
            if(intrx.equals(node))
            {
                return true;
            }
        }

        return false;
    }

    /*private GRIDroad evalMultipleEdges(){
        GRIDroad bestRoad = new GRIDroad(-1L);

        return bestRoad;
    }*/
}
