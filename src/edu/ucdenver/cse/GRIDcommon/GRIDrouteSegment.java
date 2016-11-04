package edu.ucdenver.cse.GRIDcommon;

public class GRIDrouteSegment {
    private String road_ID;
    private String intersection_ID;
    private Long timeAtRoadExit;
    private Double segmentEmissions;

    public GRIDrouteSegment() {}
    public GRIDrouteSegment(String inputRoadID, Long exitTime, Double inputEmissions) {
        this.road_ID = inputRoadID;
        this.timeAtRoadExit = exitTime;
        this.segmentEmissions = inputEmissions;
    }

    public String getRoadID() { return road_ID; }
    public Long getTimeAtRoadExit() { return timeAtRoadExit; }
    public Double getSegmentEmissions() { return this.segmentEmissions; }

    public void setRoadID(String inputRoadID) { this.road_ID = inputRoadID; }
    public void setTimeAtRoadExit(Long inputStartTime) { this.timeAtRoadExit = inputStartTime; }
    public void setSegmentEmissions(Double inputEmissions) { this.segmentEmissions = inputEmissions; }

    public String toString() {
        return this.road_ID+" (time at exit: "+this.timeAtRoadExit+"|emissions: "+this.segmentEmissions+")";
    }
}
