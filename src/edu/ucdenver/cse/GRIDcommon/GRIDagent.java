package edu.ucdenver.cse.GRIDcommon;



public class GRIDagent {
    private String Id = "";
    private GRIDroute myRoute;
    private Long x; private Long y; // Future Use for now 05/08
    private String currentLink;     // Future Use 05/08
    private String origin;
    private String destination;
    private Long fuelEconomy;
    private Long vehOccupancy;
    private Long vehSize;
    private double departureTime;
    private boolean routeHasChanged = false;
    
    // These 2 values are for matsim integration only. Could be put into extended class
    private boolean simCalcFlag      = false;       // Is this one of OUR agents, so we can change its route
    private boolean needsDestination = false;  // The event we use to create new agents does not have access to their destination

	public GRIDagent(String Id, String curLink, String origin, String destination, boolean simFlag, boolean needDest) {
        //super();

        this.Id               = Id;
        this.myRoute          = new GRIDroute();
        this.currentLink      = curLink;
        this.origin           = origin;
        this.destination      = destination;
        this.simCalcFlag      = simFlag;
        this.needsDestination = needDest;
    }
	
	public GRIDagent(String Id, String curLink, String destination) {
		this.Id          = Id;
		this.currentLink = curLink;
        this.origin      = curLink;
        this.destination = destination;

	}

    public void setRoute(GRIDroute newPath){ myRoute = newPath;}
    
    public void setX(Long x) {
        this.x = x;
    }
    public void setY(Long y) {
        this.y = y;
    }
    public void setLink(String newLink){ this.currentLink = newLink; }
    public void setOrigin(String newOrigin){ this.origin = newOrigin; }
    public void setDestination(String newDest){ this.destination = newDest; }
    public void setFuelEconomy(Long fuelEconomy){ this.fuelEconomy = fuelEconomy; }
    public void setVehOccupancy(Long vehOccupancy){ this.vehOccupancy = vehOccupancy; }
    public void setvehSize(Long vehSize){ this.vehSize = vehSize; }
	public void setDepartureTime(double departureTime) { this.departureTime = departureTime; }
	public void setRouteHasChanged(boolean routeHasChanged) { this.routeHasChanged = routeHasChanged; }
	
	// matsim integration only. Move to extended class?
	// Should we always force to false? once the destination is set, we'll never go back to needing
	public void setNeedsDestinationFlag(boolean theFlag) { this.needsDestination = theFlag; } 

    public String getId(){return this.Id;}
    public GRIDroute getRoute(){ return this.myRoute;}
    public String getCurrentLink(){ return this.currentLink; }
    public String getOrigin(){ return this.origin; }
    public String getDestination(){ return this.destination; }
    public Long getFuelEconomy(){ return this.fuelEconomy; }
    public Long getVehOccupancy(){ return this.vehOccupancy; }
    public Long getVehSize(){ return this.vehSize; }
    public double getDepartureTime() { return this.departureTime; }
	public boolean getRouteHasChanged() { return this.routeHasChanged; }
	
	// matsim integration only. Move to extended class?
	public boolean getNeedsDestination() { return this.needsDestination; }
	public boolean getSimCalcFlag() { return this.simCalcFlag; }

    @Override
    // step through both arrays in order, do something like:
    // intx - road - intx - road - intx - road etc
    // we need a method to translate intersections into roads and store the roads
    // in the GRIDroute class object
    public String toString()
    {
        for (String intx : myRoute.getIntersections())
        {

        }
        return "GRIDagent Id=" + this.Id + " Origin: " + this.origin + " Dest: " + this.destination;
    }


}