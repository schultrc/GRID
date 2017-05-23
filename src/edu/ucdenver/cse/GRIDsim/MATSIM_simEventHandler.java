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
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Route;
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

import edu.ucdenver.cse.GRIDclient.GRIDrequest;
import edu.ucdenver.cse.GRIDclient.GRIDrequestSender;
import edu.ucdenver.cse.GRIDcommon.*;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDmap.*;
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

	private TripRouter tripRouter;
	private Scenario scenario;

	MATSIM_simEventHandler(TripRouter tripRouter) {
		this.tripRouter = tripRouter;
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

				theGridAgent.setDestination(mobsimAgents.get(theGridAgent.getId()).getDestinationLinkId().toString());
				theGridAgent.setNeedsDestinationFlag(false);
			}
		}

		// Map updates - Do we need anything else from the matsim map?
		if (event.getSimulationTime() % 5 == 0) {

			NetsimNetwork thesimNetwork = mobsim.getNetsimNetwork();

			Iterator<? extends Link> iter = thesimNetwork.getNetwork().getLinks().values().iterator();

			while (iter.hasNext()) {
				Link tempLink = iter.next();
				theMap.getRoad(tempLink.getId().toString()).setCurrentSpeed(tempLink.getFreespeed());
			}

			// We should remove any info in the road.vehiclesCurrentlyOnRoad at
			// time - 1
		}

		// Agent route updates - every time
		while (!agentsToReplan.isEmpty()) {

			// We can change this by sorting the list prior to removing
			GRIDagent tempAgent = theAgents.get(agentsToReplan.remove());

			if (tempAgent != null) {
				// System.out.println("Found Agent to replan: " +
				// tempAgent.getId());
				if (mobsimAgents.containsKey(tempAgent.getId())) {
					// System.out.println("Replacing the route for agent: " +
					// tempAgent.getId());
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
			logWriter.log(Level.WARNING, "simEventHandler - ROUTE FROM SERVER NULL");

			return false;
		}

		// RCS depending on if the new alg sends complete routes, fix them here
		// Initially, routes only have intersections, so set the roads
		//newGRIDRoute.setRoads(theMap.getPathByRoad(newGRIDRoute.getIntersections()));

		// Our destination is on a road, but our calcs are based on
		// intersections.
		// Find the intersection that starts the road our destination is on

		String destinationRoad = tempGRIDagent.getDestination();
		String destinationIntersection = theMap.getRoad(destinationRoad).getFrom();

		// This can cause an out of bounds error. Check getRoads().size before accessing
		String routeLastRoad = newGRIDRoute.getRouteSegments().get(newGRIDRoute.getRouteSegments().size()-1).getRoadID();
		
		if (!destinationIntersection.equals(theMap.getRoad(routeLastRoad).getTo())) {
			// This is bad, our new route doesn't go to where our agent wants to
			// go
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
		
		//logWriter.log(Level.INFO, "NEW route is      " + newRoute.toString());
		//logWriter.log(Level.INFO, "Current index is: " + currentLinkIndex);
		//logWriter.log(Level.INFO, "Current Link is:  " + agent.getCurrentLinkId().toString());
		
		
		// NetworkRoute demoRoute = (NetworkRoute) demoLeg.getRoute();

		NetworkRoute demoNRoute = (NetworkRoute) demoLeg.getRoute();
		
		//logWriter.log(Level.INFO, "Current route is: " + demoNRoute.toString());
		
		for (int i = 0; i < currentLinkIndex; i++) {
			mobsimLinks.add(i, (Id<Link>) demoNRoute.getLinkIds().get(i));
		}

		//logWriter.log(Level.INFO, "demoNRoute is: " + mobsimLinks.toString());

		// for(String ourRoad:theRoute) {
		for (int i = 0; i < routeRoadList.size(); ++i) {
			// Add the road to the list for mobsim
			mobsimLinks.add(i + currentLinkIndex, Id.createLinkId(routeRoadList.get(i)));
		}

		demoNRoute.setLinkIds(demoLeg.getRoute().getStartLinkId(), mobsimLinks, demoLeg.getRoute().getEndLinkId());


		//logWriter.log(Level.INFO, WithinDayAgentUtils.getModifiableCurrentLeg(agent).toString());
		//logWriter.log(Level.INFO, WithinDayAgentUtils.getCurrentRouteLinkIdIndex(agent).toString());
		//logWriter.log(Level.INFO, "CurLeg 2: " + demoLeg.getRoute().toString());

		// Reset so the sim uses the new route
		WithinDayAgentUtils.resetCaches(agent);
		
		return true;
	}

	private boolean checkMatsim(MobsimAgent agent, Netsim mobsim) {

		// These checks were all recommended by MATSIM - not sure if actually
		// needed
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

			// Is this where we die in MATSIM (no route in plan) ?
			return true;
		}

		if (theMap.getRoad(tempGRIDagent.getCurrentLink()).getTo()
				.equals(theMap.getRoad(tempGRIDagent.getDestination()).getFrom())) {
			// If we can get to the start intersection of our destination link,
			// we are there. Do no replan
			// logWriter.log(Level.INFO,"Agent " + agent.getId().toString() + "
			// can no longer change route - almost there");

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

		// Not currently used. May change call to replan to here so the agents
		// haven't entered
		// the next link. Currently, we plan from the end of the the next road
		// System.out.println("We got to the beginning of
		// notifyMobsimAfterSimStep at time: " + event.getSimulationTime());

		// here is where we contact the server and let it know the time has
		// incremented
		GRIDrequestSender theRequestSender = new GRIDrequestSender();

		GRIDtimeMsg theTimeMsg = new GRIDtimeMsg((long) event.getSimulationTime());

		theRequestSender.sendRequest(theTimeMsg);
		
		//for(GRIDroad road : theMap.getRoads().values() ) {
		//	road.removeWeightAtTime((long) event.getSimulationTime());
		//}
		
		logWriter.log(Level.FINEST,
				"**************************************************************\n" + " END of SIM Time Step: "
						+ event.getSimulationTime() + "\n"
						+ "**************************************************************\n\n");
	}
}

// LEFTOVER STUFF. REMOVE WHEN HAPPY WITH ABOVE CODE

// List<Id<Link>> theList = new ArrayList<Id<Link>>();
// theList.add(Id.createLinkId("2to3"));
// theList.add(Id.createLinkId("3to8"));
// theList.add(Id.createLinkId("8to13"));
// theList.add(Id.createLinkId("13to18"));
// theList.add(Id.createLinkId("18to24"));

// if(agent.getCurrentLinkId().toString().equals("2to3")) {

// netRoute.setLinkIds(currentLeg.getRoute().getStartLinkId(),
// theList,
// currentLeg.getRoute().getEndLinkId());

// currentLeg.setRoute(netRoute);
// Reset so the sim uses the new route
// WithinDayAgentUtils.resetCaches(agent);
// }

// else {
// System.out.println("Not changing for link: " + currentLinkId);
// }

/*
 * if (ourAgents.containsKey(agent.getId().toString())) { if
 * (ourAgents.get(agent.getId().toString()).getSimCalcFlag()) {
 * theMobsimAgents.put(agent.getId().toString(), agent);
 * 
 * System.out.println("Adding agent: " + agent.getId() + " to the list"); } }
 */

// if (agent.getId().toString().equals("1")) { // some condition ...
// System.out.println("found agent" + agent.toString());
// theMobsimAgents.put("1", agent);
// }

// List<PlanElement> planElements = plan.getPlanElements();

// final Integer planElementsIndex =
// WithinDayAgentUtils.getCurrentPlanElementIndex(agent);

// System.out.println("Sim Time is: " + now);

// int currentLinkIndex =
// WithinDayAgentUtils.getCurrentRouteLinkIdIndex(withinDayAgent);

// if (!(planElements.get(planElementsIndex + 1) instanceof Activity
// || !(planElements.get(planElementsIndex + 2) instanceof Leg))) {
// log.error(
// "this version of withinday replanning cannot deal with plans where legs and
// acts do not alternate; returning ...");
// return false;
// }
// Iterator<? extends NetsimLink> iter =
// thesimNetwork.getNetsimLinks().values().iterator();

// System.out.println("We got to the begining of notifyMobsimBeforeSimStep: " +
// event.toString() + " " + event.getSimulationTime() );
// Netsim mobsim = (Netsim) event.getQueueSimulation() ;
// this.scenario = mobsim.getScenario();

// NetsimNetwork thesimNetwork = mobsim.getNetsimNetwork();

// Map<Id<Link>, NetsimLink> theLinks = (Map<Id<Link>, NetsimLink>)
// thesimNetwork.getNetsimLinks();

// Iterator iter = thesimNetwork.getNetsimLinks().values().iterator();

// for(Id<Link> roadId:theLinks.keySet()) {
// System.out.println("DAFUQ? ID=" + roadId.toString());
// System.out.println("DAFUQ? " +
// thesimNetwork.getNetsimLink(roadId).getAllNonParkedVehicles().size());

// This shows how many vehicles are on a link at any give time

// }

// this.scenario.getNetwork().getLinks()
// Map<Id<Link>, Link> theOtherLinks = (Map<Id<Link>, Link>)
// this.scenario.getNetwork().getLinks();

// for(Id<Link> roadId:theOtherLinks.keySet()) {
// System.out.println("DAFUQ? " + roadId.toString());
// System.out.println("DAFUQ? " +
// this.scenario.getNetwork().getLinks().get(roadId).getCapacity() );
// System.out.println("DAFUQ? " + roadId.toString());
// System.out.println("DAFUQ? " + roadId.toString());
// }

// for(String roadID:theMap.getRoads().keySet()) {
// System.out.println("Start: " + theMap.getRoad(roadID).getCurrentSpeed());

// theMap.getRoad(roadID).setCurrentSpeed(theMap.getRoad(roadID).getCurrentSpeed()
// + 1);
// }

// Collection<MobsimAgent> agentsToReplan = getAgentsToReplan(mobsim);
// for (MobsimAgent ma : agentsToReplan) {

// System.out.println("we found agent: " + ma.toString());

// doReplanning(ma, mobsim);
// }



////////
// More stuff from inside doReplanning - remove when done

// logWriter.log(Level.INFO,"\n\n\nAgent " +
// agent.getId().toString() + " start: " +
// tempGRIDagent.getCurrentLink() +
// " destination is: " + tempGRIDagent.getDestination());

// logWriter.log(Level.INFO,"Mobsim links are: ");
// for(Id<Link> mobsimlink:mobsimLinks) {
// logWriter.log(Level.INFO,mobsimlink.toString() + " ");
// }

// System.out.print("\n");

// System.out.println("agent is on" +
// agent.getCurrentLinkId().toString());