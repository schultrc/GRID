package edu.ucdenver.cse.GRIDmap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GRIDintersection {
	private String Id = "";
	private double x;
	private double y;
	
	// Used only when part of a server map
	private Map<String, Double> reachableDestinations;
	
	//RCS remove 
	//private Long timeAtExit;
	
    public GRIDintersection(String id, double x, double y) {
		super();
		Id = id;
		this.x = x;
		this.y = y;
		// RCS remove
		//this.timeAtExit = 0L;
	}

	public void setId(String id) { Id = id; }
	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	
	// RCS remove
	//public void setTimeAtExit(Long exitTime) { this.timeAtExit = exitTime; }

	public String getId(){return Id;}

	@Override
	public String toString() {
		return "GRIDintersection [Id=" + Id + ", x=" + x + ", y=" + y + "]";
	}

	// this method will be called by the map parser - and only when this intersection is part of a map
	// being used in a server (fib heap) instance
	// It will create a map of intersections that can be reached from this intersection and their associated
	// distances
	public void addDestination(String intersectionID, double length) {
		if (this.reachableDestinations == null) {
			this.reachableDestinations = new ConcurrentHashMap<String, Double>();
		}
		this.reachableDestinations.put(intersectionID, length);
	}
	
	public Map<String, Double> getIntersectionsFrom() {
		if (!(this.reachableDestinations == null)) {
			return this.reachableDestinations;
		}
		
		return null;
	}
	
	// RCS remove
    /*@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Id == null) ? 0 : Id.hashCode());
        return result;
    }*/

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GRIDintersection other = (GRIDintersection) obj;
        if (Id == null) {
            if (other.Id != null)
                return false;
        } else if (!Id.equals(other.Id))
            return false;
        return true;
    }
}
