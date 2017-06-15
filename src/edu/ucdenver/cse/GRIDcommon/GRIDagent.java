package edu.ucdenver.cse.GRIDcommon;



public class GRIDagent {
    private String Id = "";
    private GRIDroute newRoute;         // a new route that has been planned but not being followed, yet
    private GRIDroute currentRoute;     // the route this agent *should* be following
    
    // RCS add this functionality to track where the agents ACTUALLY went
    private GRIDroute actualRouteTaken; // add reset function, methods to add to this as we travel
    
    private Long x; private Long y; // Future Use for now 05/08
    private String currentLink;     	// The name of the road the agent is currently on 
    private String origin;
    private String destination;
    private Long fuelEconomy;
    private Long vehOccupancy;
    private Double vehSize;
    private double departureTime;
    private boolean routeHasChanged = false; // set to let the agent know it has a new route
    
    // These 2 values are for matsim integration only. Could be put into extended class
    private boolean simCalcFlag      = false;       // Is this one of OUR agents, so we can change its route
    private boolean needsDestination = false;  // The event we use to create new agents does not have access to their destination

	public GRIDagent(String Id, String curLink, String origin, String destination, boolean simFlag, boolean needDest) {
        //super();

        this.Id               = Id;
        this.currentRoute          = new GRIDroute();
        this.currentLink      = curLink;
        this.origin           = origin;
        this.destination      = destination;
        this.simCalcFlag      = simFlag;
        this.needsDestination = needDest;
        this.vehSize          = 4.5;
    }
	
	public GRIDagent(String Id, String curLink, String destination) {
		this.Id          = Id;
		this.currentLink = curLink;
        this.origin      = curLink;
        this.destination = destination;
	}

    public void setRoute(GRIDroute newPath){ currentRoute = newPath;}
    
    public void setX(Long x) {
        this.x = x;
    }
    public void setY(Long y) {
        this.y = y;
    }
    public void setLink(String newLink){ this.currentLink = newLink; }
    public void setOrigin(String newOrigin){ this.origin = newOrigin; }
    public void setDestination(String newDest){ this.destination = newDest; }
    public void setFuelEconomy(Long newFuelEconomy){ this.fuelEconomy = newFuelEconomy; }
    public void setEmissions(Double mewEmissions){ }
    public void setVehOccupancy(Long vehOccupancy){ this.vehOccupancy = vehOccupancy; }
    public void setVehSize(Double vehSize){ this.vehSize = vehSize; }
	public void setDepartureTime(Double newDepartureTime) { this.departureTime = newDepartureTime; }
	public void setRouteHasChanged(boolean hasRouteChanged) { this.routeHasChanged = hasRouteChanged; }
	
	// matsim integration only. Move to extended class?
	// Should we always force to false? once the destination is set, we'll never go back to needing
	public void setNeedsDestinationFlag(boolean theFlag) { this.needsDestination = theFlag; } 

    public String getId(){return this.Id;}
    public GRIDroute getCurrentRoute(){ return this.currentRoute;}
	public void setNewRoute(GRIDroute newRoute) { this.newRoute = newRoute;	}
	public GRIDroute getNewRoute() { return this.newRoute; }
    public String getCurrentLink(){ return this.currentLink; }
    public String getOrigin(){ return this.origin; }
    public String getDestination(){ return this.destination; }
    public Long getFuelEconomy(){ return this.fuelEconomy; }
    public Long getVehOccupancy(){ return this.vehOccupancy; }
    public Double getVehSize(){ return this.vehSize; }
    public Double getDepartureTime() { return this.departureTime; }
	public boolean getRouteHasChanged() { return this.routeHasChanged; }
	
	// matsim integration only. Move to extended class?
	public boolean getNeedsDestination() { return this.needsDestination; }
	public boolean getSimCalcFlag() { return this.simCalcFlag; }

    @Override
    public String toString()
    {  
        return "GRIDagent Id=" + this.Id + " Origin: " + this.origin + " CurrentLink: " + this.currentLink + " Dest: " + this.destination;
    }




}