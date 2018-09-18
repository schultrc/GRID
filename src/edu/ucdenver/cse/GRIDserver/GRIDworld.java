package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.*;

import java.util.concurrent.ConcurrentHashMap;

import edu.ucdenver.cse.GRIDcommon.*;

public class GRIDworld {
	private GRIDmap theMap;
	private Long theTime;
	private Integer theWeightType;
	private final ConcurrentHashMap<String, GRIDagent> masterAgents;

	public Long getTime() {
		return theTime;
	}

	public Integer getWeightType() { return theWeightType; }
	
	public void setTime(Long theTime) {
		this.theTime = theTime;
	}

	public GRIDworld(GRIDmap map, Long startTime, Integer weightType) {
		masterAgents = new ConcurrentHashMap<String, GRIDagent> ();
		this.theTime = startTime;
		this.theMap = map;
		this.theWeightType = weightType;
		
		System.out.println("GRIDworld init with: " + startTime + " and a weight value of " + weightType);
	}

	public GRIDmap getMap() {
		return theMap;
	}

	public ConcurrentHashMap<String, GRIDagent> getMasterAgents() {
		return masterAgents;
	}
	
	
}
