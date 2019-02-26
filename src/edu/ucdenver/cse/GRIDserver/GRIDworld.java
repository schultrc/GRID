package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.*;

import java.util.concurrent.ConcurrentHashMap;

import edu.ucdenver.cse.GRIDcommon.*;

// This class is a holder for the current state of the server system. 

public class GRIDworld {
	private GRIDmap theMap;
	private Long theTime;
	private final ConcurrentHashMap<String, GRIDagent> masterAgents;

	public Long getTime() {
		return theTime;
	}
	
	public void setTime(Long theTime) {
		this.theTime = theTime;
	}

	public GRIDworld(GRIDmap map, Long startTime) {
		masterAgents = new ConcurrentHashMap<String, GRIDagent> ();
		this.theTime = startTime;
		this.theMap = map;
		
		System.out.println("GRIDworld init with: " + startTime);
	}

	public GRIDmap getMap() {
		return theMap;
	}

	public ConcurrentHashMap<String, GRIDagent> getMasterAgents() {
		return masterAgents;
	}
	
	
}
