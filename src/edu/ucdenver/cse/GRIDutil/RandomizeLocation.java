package edu.ucdenver.cse.GRIDutil;

import java.util.ArrayList;
import java.util.Random;

import edu.ucdenver.cse.GRIDmap.GRIDmap;

public class RandomizeLocation 
{
	private GRIDmap myMap;
		
	public RandomizeLocation(GRIDmap theMap)
	{
		 this.myMap = theMap;
	}
	
	public ArrayList<StartToDestinationLocation> generateHomeToWorkLocations(String work_area, String home_area, int population, int randomize_type)
	{
		ArrayList<StartToDestinationLocation> trip = new ArrayList<StartToDestinationLocation>();
		
		ArrayList<String> workLinks = new ArrayList<String>();
		workLinks.addAll(myMap.getRoads().keySet());
		
		ArrayList<String> homeLinks = new ArrayList<String>();
		homeLinks.addAll(workLinks);
		
		if (randomize_type == 1)
			homeLinks = clearWorkNodesFromHomeNodes(workLinks, homeLinks);
		
		Random rnd_home_node = new Random();
		Random rnd_work_node = new Random();
				
		for(int i=0; i<population; i++)
		{
			int a = rnd_home_node.nextInt(homeLinks.size());
			int b = rnd_work_node.nextInt(workLinks.size());
			
			if (a == b) {
				i--;
			}
			
			String source_node = homeLinks.get(a);
			String destination_node = workLinks.get(b);
			
			trip.add(new StartToDestinationLocation(source_node, destination_node));
			
		}
		return trip;
	}
	
	/**
	 * removes the work area nodes from the home area nodes, and returns home area nodes
	 * */
	private static ArrayList<String> clearWorkNodesFromHomeNodes(ArrayList<String> work_links, ArrayList<String> home_links)
		{
			for(int i=0; i<work_links.size(); i++)
			{
				for(int j=0; j<home_links.size(); j++)
				{
					if(work_links.get(i).equals(home_links.get(j)))
					{
						home_links.remove(work_links.get(i));
					}
				}
			}		
			return home_links;
		}
	
	
//	public static void main(String[] args) 
//	{
//		ParseLink pn = new ParseLink();
//		RandomizeLocation rndLoc = new RandomizeLocation(pn);
//		String work_area = "./data/PubloDowntownLinks.txt";
//		String home_area = "./data/PuebloLinks.txt";
//		ArrayList<StartToDestinationLocation> trips = rndLoc.generateHomeToWorkLocations(work_area, home_area, 100);
//		for(int i=0; i<trips.size(); i++)
//		{
//			System.out.println(trips.get(i).toString());
//		}
//		
//	}
	
	
}
