package edu.ucdenver.cse.GRIDmap;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.logWriter;

public class GRIDroad {

	// Each road is a one way

	// If there are no cars, the weight should be 0
	 private static final Double ourDefaultValue = (double) 0;

	private String Id = "";
	private String to = "";
	private String from = "";

	// Defined in meters
	private double Length;

	// Defined in km/hr
	private double maxSpeed;

	// Defined in km/hr
	private double currentSpeed;

	// Use a long as the key, which represents miliseconds since midnight,
	// January 1, 1970
	
	private ConcurrentHashMap<Long, Double> vehiclesCurrentlyOnRoadAtTime;

	// RCS new data store
	private SortedMap<Long, Double> newVehiclesOnRoadAtTime;
	
	// Max capacity is defined in vehicles per hour
	private double maxCapacity;

	public GRIDroad(String roadId) {
		// start with enough space for an hour
		vehiclesCurrentlyOnRoadAtTime = new ConcurrentHashMap<Long, Double>();
		newVehiclesOnRoadAtTime = new TreeMap<Long, Double>();
		currentSpeed = -1;			
		Id = roadId;
	}
	
	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "GRIDroad [Id=" + Id + ", to=" + to + ", from=" + from + ", Length=" + Length + ", maxSpeed=" + maxSpeed
				+ ", currentSpeed=" + currentSpeed + ", roadCapacity=" + vehiclesCurrentlyOnRoadAtTime + "]";
	}

	public double getLength() {
		return Length;
	}

	public void setLength(double length) {
		Length = length;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getCurrentSpeed() {
		if (this.currentSpeed < 0) {
			return this.getMaxSpeed();
		}
		return this.currentSpeed;
	}
	
	// return the estimated speed at a given time
	public double getSpeedAtTime(long time) {
		
		// THIS IS A PLACE HOLDER - need to actually calculate the speed
		return this.getCurrentSpeed();
	}

	public void setCurrentSpeed(double currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public void removeAgentsFromRoadAtTime(long time) {

		long size = this.newVehiclesOnRoadAtTime.size();
		
		//this.vehiclesCurrentlyOnRoadAtTime.remove(time);
		
		removeOldTraffic(time);
		
		logWriter.log(Level.FINEST,  " size of roads went from: " +
				 size + " to: " + this.newVehiclesOnRoadAtTime.size() + " at time: " + time +
				 " on road: " + this.Id);
	}

	// is there a better, batch type way of doing this?
	public void addAgentsToRoadAtTime(long time) {

		if (this.newVehiclesOnRoadAtTime.containsKey(time)) {
			this.newVehiclesOnRoadAtTime.replace(time, (this.newVehiclesOnRoadAtTime.get(time) + 1));
		}
		//if (this.vehiclesCurrentlyOnRoadAtTime.containsKey(time)) {
		//	this.vehiclesCurrentlyOnRoadAtTime.replace(time, (this.vehiclesCurrentlyOnRoadAtTime.get(time) + 1));
		else {
			this.newVehiclesOnRoadAtTime.put(time, ourDefaultValue + 1);
		}
	}
	
	public void subFromWeight(long time) {
		// If there is already an offset, add to it
		if (this.newVehiclesOnRoadAtTime.containsKey(time)) {
			this.newVehiclesOnRoadAtTime.replace(time, ((this.newVehiclesOnRoadAtTime.get(time) == 0) ? 0 : this.newVehiclesOnRoadAtTime.get(time) -1));
		}

		else {
			this.newVehiclesOnRoadAtTime.put(time, ourDefaultValue);
		}
	}

	// Returns the average number of vehicles on this road between the 2 times provided
	// Currently adjusted to a per hour rate
	
	public double getAvgVehicleCount(long timeEnterRoad, long timeLeaveRoad) {
		int numberOfKeys = 0;

		double avgVehicleCount = 0.0;
		double travelTime = timeLeaveRoad - timeEnterRoad;

		for (long i = timeEnterRoad; i < timeLeaveRoad; ++i) {
			//System.out.println("i is: " + i);
			
			if ( (this.newVehiclesOnRoadAtTime.containsKey(i)) && 
			   ( !this.newVehiclesOnRoadAtTime.get(i).equals(null)) ) {
				
				avgVehicleCount += this.newVehiclesOnRoadAtTime.get(i);
				numberOfKeys++;
			}
		}

		if (numberOfKeys > 1)
			avgVehicleCount /= numberOfKeys;

		// need to adjust to be based on 1 hr
		avgVehicleCount = avgVehicleCount * (3600 / travelTime);

		// logWriter.log(Level.INFO, this.getClass().getName() + " - " +
		// " avgVehCount is: " + avgVehicleCount);
		return avgVehicleCount;
	}

	// Get the time to traverse this road at "current" time / speed
	public Long getTravelTime() {
		return Math.round(this.Length / this.getCurrentSpeed());
	}
	
	public long getTravelTime(long time) {
		return Math.round(this.Length / this.getSpeedAtTime(time));
	}
	
	public void removeOldTraffic(long time) {
		
		// Create a map that only contains the elements that are in the future (or RIGHT now)
		if(!this.newVehiclesOnRoadAtTime.tailMap(time).equals(null)) {
			SortedMap<Long, Double> tempMap = new TreeMap<Long, Double>(this.newVehiclesOnRoadAtTime.tailMap(time));
		
			this.newVehiclesOnRoadAtTime = tempMap;
		}
		
		else {
			this.newVehiclesOnRoadAtTime = new TreeMap<Long, Double>();
		}
	}	
}
