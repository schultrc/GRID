package edu.ucdenver.cse.GRIDmap;

public class GRIDintersection {
	private String Id = "";
	private double x;
	private double y;
	private Long timeAtExit;
	
    public GRIDintersection(String id, double x, double y) {
		super();
		Id = id;
		this.x = x;
		this.y = y;
		this.timeAtExit = 0L;
	}

	public void setId(String id) { Id = id; }
	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	public void setTimeAtExit(Long exitTime) { this.timeAtExit = exitTime; }

	public String getId(){return Id;}

	@Override
	public String toString() {
		return "GRIDintersection [Id=" + Id + ", x=" + x + ", y=" + y + "]";
	}

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
