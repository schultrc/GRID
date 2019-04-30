package edu.ucdenver.cse.GRIDutil;

public class StartToDestinationLocation 
{
	public String start;
	public String destination;
	
	public StartToDestinationLocation(String start, String destination)
	{
		this.start = start;
		this.destination = destination;
	}

	public String getStartLocation()
	{
		return start;
	}
	
	public String getDectinationLocation()
	{
		return destination;
	}
	
	public String toString()
	{
		return "FROM: " + start.toString() + "       TO: " + destination.toString();
	}	
}
