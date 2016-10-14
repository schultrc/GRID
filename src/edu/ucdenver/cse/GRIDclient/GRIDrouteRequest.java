package edu.ucdenver.cse.GRIDclient;

import java.io.Serializable;


public class GRIDrouteRequest  implements GRIDrequest, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public String getOrigin() {
		return location;
	}

	public void setOrigin(String origin) {
		this.location = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	private String agentID;
	private String location;
	private String destination;
	
	public GRIDrouteRequest(String ID, String origin, String destination) {
		this.agentID     = ID;
		this.location      = origin;
		this.destination = destination;
	}

	@Override
	public String toString() {
		return "GRIDrouteRequest [agentID=" + agentID + ", location=" + location + ", destination=" + destination + "]";
	}
}
