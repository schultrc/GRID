package edu.ucdenver.cse.GRIDmap;

import java.util.ArrayList;
import java.util.List;

public class GRIDintersection {
	private String Id = "";
	private double x;
	private double y;
	
	// Used only when part of a server map
	private List<String> reachableDestinations;
	
    public GRIDintersection(String id, double x, double y) {
		super();
		Id = id;
		this.x = x;
		this.y = y;
	}

	public void setId(String id) { Id = id; }
	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }

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
			this.reachableDestinations = new ArrayList<String>();
		}
		this.reachableDestinations.add(intersectionID);
	}
	
	public List<String> getIntersectionsFrom() {
		return this.reachableDestinations;	
	}
	
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
