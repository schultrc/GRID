package edu.ucdenver.cse.GRIDsim;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;

import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;

public class MATSIM_agentEventHandler implements LinkEnterEventHandler, LinkLeaveEventHandler, PersonArrivalEventHandler,
		PersonDepartureEventHandler {

	// What percent of the agents do we control?
	int controlPercent; 
	
	// The list of agents we know about
	ConcurrentHashMap<String, GRIDagent> ourAgents;
	
	// The map as we know it
	GRIDmap ourMap;
	
	double totalTravelTime = 0;
	
	Queue<String> agentsToReplan;

	public MATSIM_agentEventHandler(int percent) {
		if (percent > 100) {
			logWriter.log(Level.WARNING, this.getClass() + 
					"Percentage must be >= 0 and < 100");
		}
		
		this.controlPercent = percent;
	}
	
	// This should NEVER be called
	public Queue<String> getAgentsToReplan() { return agentsToReplan; }

	public void setAgentsToReplan(Queue<String> agentsToReplan) {
		this.agentsToReplan = agentsToReplan;
	}

	public double getTotalTravelTime() { return totalTravelTime; }

	// This should NEVER get called
	public void setTotalTravelTime(double totalTravelTime) {
		this.totalTravelTime = totalTravelTime;
	}

	// This should NEVER get called
	public GRIDmap getOurMap() { return ourMap; }

	public void setOurMap(GRIDmap ourMap) { this.ourMap = ourMap; }

	public ConcurrentMap<String, GRIDagent> getOurAgents() { return ourAgents; }

	public void setOurAgents(ConcurrentHashMap<String, GRIDagent> myAgents) {
		this.ourAgents = myAgents;
	}

	@Override	
	public void reset(int iteration) {
		// Do we care if this ever happens?
		System.out.println("EVENT reset happened. ? ");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handleEvent(LinkEnterEvent event) {
			
		// Tell our agent where it is
		ourAgents.get(event.getPersonId().toString()).setLink(event.getLinkId().toString());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handleEvent(LinkLeaveEvent event) {

		GRIDagent tempAgent = ourAgents.get(event.getPersonId().toString());
		
		if (tempAgent != null) {

			// Check if this is OUR agent, and if so, add it to the replanning agents
			if (tempAgent.getSimCalcFlag()) {
				agentsToReplan.add(tempAgent.getId());
			}
			else {
				// We are good to continue our original route
			}				
		}
		else {
			// This is bad, an agent we don't know about just left a link
			logWriter.log(Level.WARNING, "Agent: " + event.getPersonId().toString() + " just left a link, but we don't know that agent!");
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {

		if( ourAgents.containsKey(event.getPersonId().toString())) {
			GRIDagent tempAgent = ourAgents.get(event.getPersonId().toString());
			
			double departureTime = tempAgent.getDepartureTime();			
			double travelTime = event.getTime() - departureTime;

			totalTravelTime += travelTime;
			ourAgents.remove(event.getPersonId().toString());
			
			logWriter.log(Level.INFO, "Agent: " + tempAgent.getId() +
					      " departed at: "      + tempAgent.getDepartureTime() + 
					      " and arrived at: "   + event.getTime());
			
		}
		
		else {
			logWriter.log(Level.WARNING, "ERROR!!! Attempt to remove an agent: " + 
		                  event.getPersonId() + " that never started!!");
		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {	
		
		boolean simFlag;
		
		double theId = (Double.parseDouble(event.getPersonId().toString()));
		
		int result = (int) (theId % 100);
		
		if(result < this.controlPercent) { 
			simFlag = true;
		}
		
		else {
			simFlag = false;
		}
		
		logWriter.log(Level.INFO, this.getClass().getName() + " setting simFlag to: " +
		                          simFlag + " for agent: " + theId);
		
		GRIDagent newAgent = new GRIDagent(event.getPersonId().toString(),
										   event.getLinkId().toString(),
				                           event.getLinkId().toString(),
				                           "", simFlag, true );  

		// Here, we need to create a new agent. 
		newAgent.setDepartureTime(event.getTime());			
		ourAgents.put(newAgent.getId(), newAgent);
	}
}

