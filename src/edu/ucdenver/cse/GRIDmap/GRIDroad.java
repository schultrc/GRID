package edu.ucdenver.cse.GRIDmap;


import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDserver.GRIDutilityFunction;

import java.util.LinkedList;
import java.util.List;

public class GRIDroad {
	
	// Each road is a one way
	
	// If there are no cars, the weight should be 0
	private static final Double ourDefaultValue = (double) 0;
	private static final Double MAX_WEIGHT = 2000000.0;

	private String Id   = "";
	private String to   = "";
	private String from = "";
	
	// Defined in meters
	private double Length;
		
	// Defined in km/hr
	private double maxSpeed;

	// Defined in km/hr
	private double currentSpeed = -1;

	// Use a long as the key, which represents miliseconds since midnight, January 1, 1970
	private ConcurrentHashMap<Long, Double> vehiclesCurrentlyOnRoadAtTime = new ConcurrentHashMap<Long, Double>();
	private ConcurrentHashMap<Long, Double> emissionsCurrentlyOnRoadAtTime = new ConcurrentHashMap<>();
	
	// Max capacity is defined in vehicles per hour
	private double maxCapacity;
	
	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public GRIDroad(String theId) { Id = theId; }
	
	public String getId() { return Id; 	}

	public void setId(String id) { Id = id; }
	
	public String getFrom() { return from; }

	public void setFrom(String from) { this.from = from; }

	public String getTo() { return to; }

	public void setTo(String to) { this.to = to; }

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
	
	// Not  currently used
	//public void calcCurrentSpeed(Long intervalStartTime) {
	//	double carLengths = 0.0,
	//		   speedModifier = 1.0;

	//	carLengths = this.getAvgVehicleCount(intervalStartTime)*4.5;
	//	speedModifier = carLengths/this.getLength();

	//	if(speedModifier > 0.8) {
	//		this.currentSpeed = this.currentSpeed/3.0;
	//	}
	//}
	
	public void removeWeightAtTime(Long time) {
		
		//logWriter.log(Level.INFO, this.getClass().getName() + " size of roads is: " + 
		//                         this.vehiclesCurrentlyOnRoadAtTime.size() + " at time: " + time +
		//                          " on road: " + this.Id);

		this.vehiclesCurrentlyOnRoadAtTime.remove(time);

	}
	
	public void addToWeight(Long time) {
		// If there is already an offset, add to it
		if(this.vehiclesCurrentlyOnRoadAtTime.containsKey(time)) {
			//System.out.println("\nREVISED: "+this.vehiclesCurrentlyOnRoadAtTime.get(time)+"\n");
			this.vehiclesCurrentlyOnRoadAtTime.replace(time, (this.vehiclesCurrentlyOnRoadAtTime.get(time) + 1));
		}
		else {
			//System.out.println("\nNEW: "+this.vehiclesCurrentlyOnRoadAtTime.get(time)+"\n");
			this.vehiclesCurrentlyOnRoadAtTime.put(time, ourDefaultValue + 1);
		}
	}
	
	public void addEmmissionsAtTime(Long time, Double emissions) {
		if(this.emissionsCurrentlyOnRoadAtTime.containsKey(time)) {
			this.emissionsCurrentlyOnRoadAtTime.replace(time,
					this.emissionsCurrentlyOnRoadAtTime.get(time) + emissions);
		}
		else {
			this.emissionsCurrentlyOnRoadAtTime.put(time, emissions);
		}
	}
	
	public void subFromWeight(Long time) {
		// If there is already an offset, add to it
		
		if(this.vehiclesCurrentlyOnRoadAtTime.containsKey(time)) {
			this.vehiclesCurrentlyOnRoadAtTime.replace(time, (this.vehiclesCurrentlyOnRoadAtTime.get(time) - 1));
			if (this.vehiclesCurrentlyOnRoadAtTime.get(time) < 0) {
				this.vehiclesCurrentlyOnRoadAtTime.replace(time, 0.0);
			}
		}
		else {
			// This should never happen
			//System.out.println("\nSHOULD NOT HAPPEN: "+time+"\n");
			this.vehiclesCurrentlyOnRoadAtTime.put(time, 0.0);
		}
	}
	
	public void subEmmissionsAtTime(Long time, Double emissions) {
		if(this.emissionsCurrentlyOnRoadAtTime.containsKey(time)) {
			this.emissionsCurrentlyOnRoadAtTime.replace(time,
					this.emissionsCurrentlyOnRoadAtTime.get(time) - emissions);
			if (this.emissionsCurrentlyOnRoadAtTime.get(time) < 0) {
				this.emissionsCurrentlyOnRoadAtTime.replace(time, 0.0);
			}
		}
		else {
			// This should never happen
			this.emissionsCurrentlyOnRoadAtTime.put(time, 0.0);
		}
	}
	
	// Not currently in use
	//public double getWeightAtTime(Long time) {
	//	if (this.vehiclesCurrentlyOnRoadAtTime.containsKey(time) ) {
	//		return this.vehiclesCurrentlyOnRoadAtTime.get(time) + this.getDefaultWeight();
	//	}
		
	//	return this.getDefaultWeight();
	//}
	// Not currently in use
	//public double getEmissionsAtTime(Long time) {
	//	if (this.emissionsCurrentlyOnRoadAtTime.containsKey(time) ) {
	//		return this.emissionsCurrentlyOnRoadAtTime.get(time) + this.getDefaultWeight();
	//	}

	//	return 0.0; // is there a default emissions level...?
	//}
	
	public double getTimeWeightOverInterval(Long intervalStartTime)
	{ // vehiclesCurrentlyOnRoad
		Double timeWeight = 0.0,
			   capMinusActual = this.maxCapacity - this.getAvgVehicleCount(intervalStartTime);
		
		/*System.out.println("\nmaxCAPACITY: "+capMinusActual+"\n");
		System.out.println("\nAVG: "+this.getAvgVehicleCount(intervalStartTime)+"\n");
		System.out.println("\nCAPACITY: "+capMinusActual+"\n");*/
		
		if(getCurrentSpeed() == 0)
			return MAX_WEIGHT;

		//calcCurrentSpeed(intervalStartTime);
		
		if(capMinusActual <= 0.0) {
			timeWeight = this.Length/this.getCurrentSpeed();
		}
		else {
			timeWeight = this.Length/(this.getCurrentSpeed()*capMinusActual);
		}
		return timeWeight;
	}
	
	
	
	public double getEmissionsWeightOverInterval(Long intervalStartTime) {
		Double emissionsWeight = 0.0,
			   avgEmissions = this.getAvgEmissions(intervalStartTime);
		GRIDutilityFunction calculator = new GRIDutilityFunction();

		emissionsWeight += calculator.calcEmissions(this.getCurrentSpeed(),this.getLength()) + avgEmissions;

		return emissionsWeight;
	}
	
	public boolean setWeightAtTime(Long time, double capacity) {
		if (this.vehiclesCurrentlyOnRoadAtTime.containsKey(time)) {
			
			// RCS Fix to log
			System.out.println("ERROR: Time already has a value for: " +
		                       this.Id + " at time: " +
							   time.toString());	

			return false;
		}
		else {
			this.vehiclesCurrentlyOnRoadAtTime.put(time, capacity);
			return true;
		}			
	}
	
	private double getDefaultWeight() {
		double theWeight;
		
		// using maxSpeed. Should this be currentSpeed???
		theWeight = this.Length / (this.maxSpeed*this.maxCapacity);
		
		return theWeight; }

	private double getAvgVehicleCount(Long timeEnterRoad){
		int numberOfKeys = 0;
		
		double avgVehicleCount = 0.0;
		double travelTime = this.Length/this.getCurrentSpeed();
		double timeLeaveRoad = timeEnterRoad + travelTime;

		for(long i = timeEnterRoad; i < timeLeaveRoad; i++)
		{
			if(this.vehiclesCurrentlyOnRoadAtTime.containsKey(i)) {
				avgVehicleCount += this.vehiclesCurrentlyOnRoadAtTime.get(i);
				numberOfKeys++;
			}
		}

		if( numberOfKeys > 1 )
			avgVehicleCount /= numberOfKeys;
		
		// need to adjust to be based on 1 hr
		
		avgVehicleCount = avgVehicleCount*(3600/travelTime);
		
		//logWriter.log(Level.INFO, this.getClass().getName() + " - " + 
		//                          " avgVehCount is: " + avgVehicleCount);
		return avgVehicleCount;
	}

	private double getAvgEmissions(Long intervalStartTime){
		int numberOfKeys = 0;
		Double avgEmissions = 0.0,
				timeOnLink = this.Length/this.getCurrentSpeed(),
				timeInterval = intervalStartTime + timeOnLink;

		for(Long i = intervalStartTime; i < timeInterval; i++)
		{
			if(this.emissionsCurrentlyOnRoadAtTime.containsKey(i)) {
				avgEmissions += this.emissionsCurrentlyOnRoadAtTime.get(i);
				numberOfKeys++;
			}
		}

		if( numberOfKeys > 1 )
			avgEmissions /= numberOfKeys;

		return avgEmissions;
	}

	public Long getTravelTime()
	{
		return Math.round(this.Length/this.getCurrentSpeed());
	}


	public void fillRoadWeight(int rdID) // ConcurrentHashMap<Long, Double>
	{
		List<Long> weights = new LinkedList<>();
		ConcurrentHashMap<Long,Double> weightMap = new ConcurrentHashMap<>();

		for(Long i=0L; i<5000; i++)
		{
			if(rdID==10)
				weightMap.put(i,5.0+i);
			if(rdID==11)
				weightMap.put(i,5000.0+i);
			if(rdID==12)
				weightMap.put(i,1.0+i);
		}

		this.vehiclesCurrentlyOnRoadAtTime = weightMap;
	}
}
