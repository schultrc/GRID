package edu.ucdenver.cse.GRIDcommon;

import java.io.Serializable;

public class GRIDrouteRequest implements Serializable {
	private String agentID;
	private String origin;
	private String destination;
	
	public GRIDrouteRequest(String ID, String origin, String destination) {
		this.agentID     = ID;
		this.origin      = origin;
		this.destination = destination;
	}

	@Override
	public String toString() {
		return "GRIDrouteRequest [agentID=" + agentID + ", origin=" + origin + ", destination=" + destination + "]";
	}
}
