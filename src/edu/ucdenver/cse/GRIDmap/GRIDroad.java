package edu.ucdenver.cse.GRIDmap;

import java.util.concurrent.ConcurrentHashMap;
import edu.ucdenver.cse.GRIDcommon.logWriter;

public class GRIDroad {

	// Each road is a one way

	// If there are no cars, the weight should be 0
	private static final Double ourDefaultValue = (double) 0;

	//private static final Double MAX_WEIGHT = 2000000.0;

	private String Id = "";
	private String to = "";
	private String from = "";

	// Defined in meters
	private double Length;

	// Defined in km/hr
	private double maxSpeed;

	// Defined in km/hr
	private double currentSpeed = -1;

	// Use a long as the key, which represents miliseconds since midnight,
	// January 1, 1970
	private ConcurrentHashMap<Long, Double> vehiclesCurrentlyOnRoadAtTime = new ConcurrentHashMap<Long, Double>();

	// Max capacity is defined in vehicles per hour
	private double maxCapacity;

	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public GRIDroad(String theId) {
		Id = theId;
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

	public void setCurrentSpeed(double currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	// Not currently used
	// public void calcCurrentSpeed(Long intervalStartTime) {
	// double carLengths = 0.0,
	// speedModifier = 1.0;

	// carLengths = this.getAvgVehicleCount(intervalStartTime)*4.5;
	// speedModifier = carLengths/this.getLength();

	// if(speedModifier > 0.8) {
	// this.currentSpeed = this.currentSpeed/3.0;
	// }
	// }

	public void removeAgentsFromRoadAtTime(Long time) {

		// logWriter.log(Level.INFO, this.getClass().getName() + " size of roads
		// is: " +
		// this.vehiclesCurrentlyOnRoadAtTime.size() + " at time: " + time +
		// " on road: " + this.Id);

		this.vehiclesCurrentlyOnRoadAtTime.remove(time);

	}

	public void addAgentsToRoadAtTime(Long time) {
		// If there is already an offset, add to it
		if (this.vehiclesCurrentlyOnRoadAtTime.containsKey(time)) {
			this.vehiclesCurrentlyOnRoadAtTime.replace(time, (this.vehiclesCurrentlyOnRoadAtTime.get(time) + 1));
		} else {
			this.vehiclesCurrentlyOnRoadAtTime.put(time, ourDefaultValue + 1);
		}
	}

	public void subFromWeight(Long time) {
		// If there is already an offset, add to it

		if (this.vehiclesCurrentlyOnRoadAtTime.containsKey(time)) {
			this.vehiclesCurrentlyOnRoadAtTime.replace(time, (this.vehiclesCurrentlyOnRoadAtTime.get(time) - 1));
			if (this.vehiclesCurrentlyOnRoadAtTime.get(time) < 0) {
				this.vehiclesCurrentlyOnRoadAtTime.replace(time, 0.0);
			}
		} else {
			this.vehiclesCurrentlyOnRoadAtTime.put(time, 0.0);
		}
	}

	// Returns the average number of vehicles on this road between the 2 times provided
	// Currently adjusted to a per hour rate
	
	public double getAvgVehicleCount(long timeEnterRoad, long timeLeaveRoad) {
		int numberOfKeys = 0;

		double avgVehicleCount = 0.0;
		double travelTime = timeLeaveRoad - timeEnterRoad;

		for (long i = timeEnterRoad; i < timeLeaveRoad; i++) {
			if (this.vehiclesCurrentlyOnRoadAtTime.containsKey(i)) {
				avgVehicleCount += this.vehiclesCurrentlyOnRoadAtTime.get(i);
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

	public Long getTravelTime() {
		return Math.round(this.Length / this.getCurrentSpeed());
	}
}
