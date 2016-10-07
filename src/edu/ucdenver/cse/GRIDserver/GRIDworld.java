package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.*;

import java.util.concurrent.ConcurrentHashMap;

import edu.ucdenver.cse.GRIDcommon.*;

public class GRIDworld {
	private GRIDmap theMap;
	private final ConcurrentHashMap<String, GRIDagent> masterAgents;
	
	public GRIDworld(GRIDmap map) {
		masterAgents = new ConcurrentHashMap<String, GRIDagent> ();
		theMap = map;
	}

	public GRIDmap getTheMap() {
		return theMap;
	}

	public ConcurrentHashMap<String, GRIDagent> getMasterAgents() {
		return masterAgents;
	}
	
	
}
