package edu.ucdenver.cse.GRIDutil;

import java.util.ArrayList;
import java.util.Random;

public class DriversDistributionOnRoad 
{
	private double[] hourly_traffic_distribution;
	private int [] hours_in_seconds;
	private static Random rnd;
	
	public DriversDistributionOnRoad(double[] hourly_traffic_distribution)
	{
		this.hourly_traffic_distribution = hourly_traffic_distribution;
		hours_in_seconds = new int[24];
		for(int i=0; i<24; i++)
			hours_in_seconds[i] = (i+1)*60*60;
		rnd = new Random();
		
	}
	
	/**calculateDistribution: calculate the distribution based on the number of drivers and the range given
	 *@param drivers: number of drivers on the road
	 *@param range: the representation of hours, can take values from 1 to 24.
	 * */
	public double[] calculateDistribution(int drivers, int range)
	{
		double[] drivers_on_road_hourly = new double[24];
			
			for(int i=0; i<hourly_traffic_distribution.length; i++)
			{
				for(int j=0; j<24; j++)
				{
					drivers_on_road_hourly[i] = (int) (drivers * hourly_traffic_distribution[i])/100;
				}
			}
	return divideOnRange(drivers_on_road_hourly, range);
	}

	/**divideOnRage: divides a given array to intervals of values
	 * @param drivers_on_road_hourly: hour-based number of drivers
	 * @range the representation range
	 * */
	private double[] divideOnRange(double[] drivers_on_road_hourly, int range)
	{
		int size = drivers_on_road_hourly.length;
		
		if(range>0 && range <=size && size%range == 0)
			{
				double[] ranged_data = new double[drivers_on_road_hourly.length/range]; 
				int begin=0;
				int end = range; 
				
				for(int j=0; j<drivers_on_road_hourly.length/range; j++)
					{
						for(int i=begin; i<end; i++)
							ranged_data[j] = ranged_data[j] + drivers_on_road_hourly[i];
												
						begin = end;
						end += range;
					}
				return ranged_data;
			}
		else
		{	return null;  }
			
	}
	
	public double sum(double [] data)
	{
		double sum = 0;
		for(int i=0; i<data.length; i++)
		{
			sum = sum + data[i];
		}
		return sum;
	}
	
	public ArrayList<Integer> generateTimes(double[] drivers_on_road_hourly )
	{
		ArrayList<Integer> times = new ArrayList<Integer>();
		int index = 0;
		for (int i = 0; i < drivers_on_road_hourly.length; i++) 
		{
			while(index < drivers_on_road_hourly[i])  //ex. 0 < 250
			{
				times.add(generateRandom((i+1)*60*60, (i+2)*60*60, rnd));
				index++;
			}
			
			index = 0;	
		}
		
		return times;
	}
	
	public int generateRandom(int min, int max, Random rnd)
	{		
		return rnd.nextInt(max - min + 1) + min; 
	}
		
	// Test stub
	public static void main(String[] args) 
	{
		double [] hour = {2.33, 2.33, 2.33, 2.33, 2.33, 2.33, 3.68, 6.00, 7.68, 6.68, 5.33, 4.33, 4.00, 3.68, 4.33, 5.68, 7.33, 5.63, 5.00, 4.68, 4.00, 3.33, 2.33, 2.33};
		DriversDistributionOnRoad d = new DriversDistributionOnRoad(hour);
		
		int range = 1;
		int drivers = 150000;
		double [] drivers_on_road_hourly = d.calculateDistribution(drivers, range);
		
		ArrayList<Integer> times = d.generateTimes(drivers_on_road_hourly);
		System.out.println(times);
		System.out.println(times.size());
		System.out.println("Sum: " + d.sum(drivers_on_road_hourly));
	}
}
