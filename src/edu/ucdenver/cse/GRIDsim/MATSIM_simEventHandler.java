package edu.ucdenver.cse.GRIDsim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.events.MobsimAfterSimStepEvent;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimAfterSimStepListener;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimLink;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimNetwork;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripRouter;

import edu.ucdenver.cse.GRIDclient.GRIDrequestSender;
import edu.ucdenver.cse.GRIDcommon.*;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDmessages.GRIDrequest;
import edu.ucdenver.cse.GRIDmessages.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDmessages.GRIDtimeMsg;

public class MATSIM_simEventHandler implements MobsimBeforeSimStepListener, MobsimAfterSimStepListener {

	MATSIM_simEventHandler() {
		
	}

	GRIDmap theMap;

	ConcurrentHashMap<String, GRIDagent> theAgents;
	Queue<String> agentsToReplan;

	// Should NEVER be called
	public Queue<String> getAgentsToReplan() {
		return agentsToReplan;
	}

	public void setAgentsToReplan(Queue<String> agentsToReplan) {
		this.agentsToReplan = agentsToReplan;
	}

	// This should NEVER get called
	public GRIDmap getTheMap() {
		return theMap;
	}

	public void setTheMap(GRIDmap theMap) {
		this.theMap = theMap;
	}

	// This should NEVER get called
	public ConcurrentMap<String, GRIDagent> getTheAgents() {
		return theAgents;
	}

	public void setTheAgents(ConcurrentHashMap<String, GRIDagent> theAgents) {
		this.theAgents = theAgents;
	}

	// How do we use this? Can we make our own?
	private static final Logger log = Logger.getLogger("dummy");

	MATSIM_simEventHandler(TripRouter tripRouter) {
	}

	@Override
	public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent event) {

		logWriter.log(Level.INFO, "notifyMobsimBeforeSimStep " + event.toString() + " " + event.getSimulationTime());

		Netsim mobsim = (Netsim) event.getQueueSimulation();

		ConcurrentHashMap<String, MobsimAgent> mobsimAgents = getAgentsToReplan(mobsim, theAgents);

		// Consider changing this to the same model as replanning, where agents
		// get added to
		// a list, instead of checking all of them every time

		// Need to update any agent that doesn't have a destination
		for (GRIDagent theGridAgent : theAgents.values()) {
			if (theGridAgent.getNeedsDestination()) {

				// RCS remove once fixed
				logWriter.log(Level.INFO, "Agent: " + theGridAgent.getId() + " still needs its destination set!");
				
				theGridAgent.setDestination(mobsimAgents.get(theGridAgent.getId()).getDestinationLinkId().toString());
				theGridAgent.setNeedsDestinationFlag(false);
			}
		}

		// Make this a configurable value, and also track it's time
		
		// This currently doesn't do anything - MATSIM does not update the speed of the links
		// Make our own speed calc?
		
		if (event.getSimulationTime() % 5 == 0) {

			NetsimNetwork thesimNetwork = mobsim.getNetsimNetwork();

			Iterator<? extends Link> iter = thesimNetwork.getNetwork().getLinks().values().iterator();

			while (iter.hasNext()) {
				Link tempLink = iter.next();
				theMap.getRoad(tempLink.getId().toString()).setCurrentSpeed(tempLink.getFreespeed());
			}
		}

		// Agent route updates - every time
		while (!agentsToReplan.isEmpty()) {

			// We can change this by sorting the list prior to removing
			GRIDagent tempAgent = theAgents.get(agentsToReplan.remove());

			if (tempAgent != null) {
				if (mobsimAgents.containsKey(tempAgent.getId())) {
					if (!doReplanning(mobsimAgents.get(tempAgent.getId()), mobsim, tempAgent.getCurrentLink())) {
						logWriter.log(Level.WARNING, "Agent: " + tempAgent.getId() + 
								                     " failed replanning at time: " + event.getSimulationTime());
					}
				}
			}
		}
	}

	private boolean doReplanning(MobsimAgent agent, Netsim mobsim, String currentLinkId) {

		// Make sure MATSIM is happy
		if (!checkMatsim(agent, mobsim)) {
			logWriter.log(Level.WARNING, "MATSIM agent cannot be modified");
			return false;
		}

		GRIDagent tempGRIDagent = theAgents.get(agent.getId().toString());

		if (weAreThere(tempGRIDagent)) {
			return true;
		}
		
		// Keep the original so we can determine if it has changed
		GRIDroute origGRIDroute = tempGRIDagent.getCurrentRoute();

		// contact the server for a new route
		GRIDrequestSender theRequestSender = new GRIDrequestSender();

		GRIDrequest testReq = new GRIDrouteRequest(agent.getId().toString(), currentLinkId,
				tempGRIDagent.getDestination());

		GRIDroute newGRIDRoute = (GRIDroute) theRequestSender.sendRequest(testReq);

		if (newGRIDRoute == null) {
			logWriter.log(Level.WARNING, "simEventHandler - ROUTE FROM SERVER NULL - agent: " + agent.getId());

			return false;
		}

		// Our destination is on a road, but our algorithm is based on intersections.
		// Find the intersection that starts the road our destination is on

		String destinationRoad = tempGRIDagent.getDestination();
		String destinationIntersection = theMap.getRoad(destinationRoad).getFrom();

		// This can cause an out of bounds error. Check getRoads().size before accessing
		if (newGRIDRoute.getRouteSegments().size() < 1) {
			logWriter.log(Level.WARNING, "ERROR: attempting to get the last road on a route with no segments");
			return false;
		}
		
		String routeLastRoad = newGRIDRoute.getRouteSegments().get(newGRIDRoute.getRouteSegments().size()-1).getRoadID();
		
		if (!destinationIntersection.equals(theMap.getRoad(routeLastRoad).getTo())) {
			
			// This is bad, our new route doesn't go to where our agent wants to go
			logWriter.log(Level.WARNING, "ERROR: Cannot get to INT: " + destinationIntersection);
			System.out.println("CANT RE-ROUTE");
			return false;
		}

		// Compare the 2 routes
		if (origGRIDroute.compare(newGRIDRoute)) {
			
			logWriter.log(Level.INFO, "Routes did not change for agent: " + agent.getId());
			return false;
		}

		else {
			// Add the new route to our agent
			tempGRIDagent.setNewRoute(newGRIDRoute);
			
			if (replaceRoute(agent, newGRIDRoute) ) {
				logWriter.log(Level.INFO, "Successfully replaced route for agent: " + agent.getId().toString());
			}
		}

		tempGRIDagent.setRoute(tempGRIDagent.getNewRoute());
		return true;
	}

	private boolean replaceRoute(MobsimAgent agent, GRIDroute newRoute) {

		Leg demoLeg = WithinDayAgentUtils.getModifiableCurrentLeg(agent);
		int currentLinkIndex = WithinDayAgentUtils.getCurrentRouteLinkIdIndex(agent);
		
		ArrayList<String> routeRoadList = newRoute.getRoads();
		List<Id<Link>> mobsimLinks = new ArrayList<Id<Link>>();
		
		NetworkRoute demoNRoute = (NetworkRoute) demoLeg.getRoute();
				
		for (int i = 0; i < currentLinkIndex; i++) {
			mobsimLinks.add(i, (Id<Link>) demoNRoute.getLinkIds().get(i));
		}

		for (int i = 0; i < routeRoadList.size(); ++i) {
			// Add the road to the list for mobsim
			mobsimLinks.add(i + currentLinkIndex, Id.createLinkId(routeRoadList.get(i)));
		}

		demoNRoute.setLinkIds(demoLeg.getRoute().getStartLinkId(), mobsimLinks, demoLeg.getRoute().getEndLinkId());

		// Reset so the sim uses the new route
		WithinDayAgentUtils.resetCaches(agent);
		
		return true;
	}

	private boolean checkMatsim(MobsimAgent agent, Netsim mobsim) {

		// These checks were all recommended by MATSIM - not sure if actually needed
		Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);

		if (plan == null) {
			log.info(" we don't have a modifiable plan; aborting ... ");

			return false;
		}

		if (!(WithinDayAgentUtils.getCurrentPlanElement(agent) instanceof Leg)) {
			log.info("agent not on leg; aborting ... ");
			System.out.println("NOT LEG");

			return false;
		}

		if (!((Leg) WithinDayAgentUtils.getCurrentPlanElement(agent)).getMode().equals(TransportMode.car)) {
			log.info("not a car leg; can only replan car legs; aborting ... ");
			System.out.println("NOT CAR");

			return false;
		}

		// everything must be ok
		return true;
	}
	
	private boolean weAreThere(GRIDagent tempGRIDagent) {
		if (tempGRIDagent.getCurrentLink().equals(tempGRIDagent.getDestination())) {
			// We must already be at our destination!

			logWriter.log(Level.INFO,
					"Agent " + tempGRIDagent.getId() + " has arrived at its destination" );

			return true;
		}

		if (theMap.getRoad(tempGRIDagent.getCurrentLink()).getTo()
				.equals(theMap.getRoad(tempGRIDagent.getDestination()).getFrom())) {
			// If we can get to the start intersection of our destination link, do no re-plan

			return true;
		}
		return false;
	}

	private static ConcurrentHashMap<String, MobsimAgent> getAgentsToReplan(Netsim mobsim,
			ConcurrentHashMap<String, GRIDagent> theAgents) {

		ConcurrentHashMap<String, MobsimAgent> theMobsimAgents = new ConcurrentHashMap<String, MobsimAgent>();

		// find agents that are en-route
		for (NetsimLink link : mobsim.getNetsimNetwork().getNetsimLinks().values()) {
			for (MobsimVehicle vehicle : link.getAllNonParkedVehicles()) {
				MobsimDriverAgent agent = vehicle.getDriver();

				theMobsimAgents.put(agent.getId().toString(), agent);
			}
		}

		return theMobsimAgents;
	}

	@Override
	public void notifyMobsimAfterSimStep(@SuppressWarnings("rawtypes") MobsimAfterSimStepEvent event) {

		// here is where we contact the server and let it know the time has incremented
		GRIDrequestSender theRequestSender = new GRIDrequestSender();

		GRIDtimeMsg theTimeMsg = new GRIDtimeMsg((long) event.getSimulationTime());

		theRequestSender.sendRequest(theTimeMsg);
		
		logWriter.log(Level.FINEST,
				"**************************************************************\n" + " END of SIM Time Step: "
						+ event.getSimulationTime() + "\n"
						+ "**************************************************************\n\n");
	}
}
