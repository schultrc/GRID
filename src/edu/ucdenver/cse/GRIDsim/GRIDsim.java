/**
 * 
 */
/**
 * @author Ray
 *
 */
package edu.ucdenver.cse.GRIDsim;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.junit.Assert;
//import org.matsim.api.core.*;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.*;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollector;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimUtils;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripRouterProviderImpl;
import com.google.inject.Provider;

import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDutil.FileUtils;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.logWriter;

public class GRIDsim {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/* This is the primary interface between the GRID algorithm and MATSIM
		 * It will create the controller for matsim as well as the MAP and other
		 * data sources needed for GRID.
		 * 
		 */
		
		Long startTime = System.currentTimeMillis();
		
		System.out.println("It's not hard--you just gotta use finesse!");
				
		logWriter.log(Level.INFO, "Starting SIM");
		
		
		// This will get filled and emptied via departure / arrival events
		final ConcurrentHashMap<String, GRIDagent> masterAgents = new ConcurrentHashMap<String, GRIDagent> ();	
		final ConcurrentHashMap<String, GRIDagent> agentsNeedingDest = new ConcurrentHashMap<String, GRIDagent> ();	

		final Queue<String> agentsToReplan = new LinkedList<String>();
		double totalTravelTime = 0;
		
		// The official map
		final GRIDmap ourMap;
		
		// Load our version of the map first
		GRIDmapReader masterMap = new GRIDmapReader();
				
		String configFile = FileUtils.getConfigFile();
	        
	    if (configFile == "") {
	    	logWriter.log(Level.WARNING, "You didn't choose a config file!!!");
	    	System.exit(0);
	    }
	    
	    logWriter.log(Level.INFO, "CONFIG FILE: " + configFile + " in use\n\n\n");
				
		try {
			Config config = new Config();
			
			ConfigUtils.loadConfig(config, configFile);
			
			config.controler().setLastIteration(0);
			config.controler().setOutputDirectory("./output");
			
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

			Scenario scenario = ScenarioUtils.loadScenario(config) ;

			final Controler controler = new Controler( scenario ) ;
			
			// Uncomment if you want to select a different map than matsim is using
			// String mapFile = GRIDutils.getConfigFile();
			String mapFile = config.network().getInputFile();

			System.out.println("File Chosen: " + mapFile);
			ourMap = masterMap.readMapFile(mapFile);
			

			// From WithinDayReplanning
			Set<String> analyzedModes = new HashSet<String>();
			analyzedModes.add(TransportMode.car);
			final TravelTimeCollector travelTime = new TravelTimeCollector(controler.getScenario(), analyzedModes);
			controler.getEvents().addHandler(travelTime);
			// end WithinDayReplanning
			
			// Add our handler for Link Events			
			MATSIM_agentEventHandler theAgentHandler = new MATSIM_agentEventHandler();
			theAgentHandler.setOurMap(ourMap);
			theAgentHandler.setOurAgents(masterAgents);
			theAgentHandler.setAgentsToReplan(agentsToReplan);
			
			controler.getEvents().addHandler(theAgentHandler);
			
			
			// Add listeners for the sim steps
			controler.addOverridingModule(new AbstractModule() {
				
				@Override
			    public void install() {
					
					this.bindMobsim().toProvider(new Provider<Mobsim>() {
						public Mobsim get() {
							// construct necessary trip router:
							TripRouter router = new TripRouterProviderImpl(controler.getScenario(),
									controler.getTravelDisutilityFactory(), travelTime,
									controler.getLeastCostPathCalculatorFactory(), controler.getTransitRouterFactory()).get();

							// construct qsim and insert listeners:
							QSim qSim = QSimUtils.createDefaultQSim(controler.getScenario(), controler.getEvents());
							
							MATSIM_simEventHandler theSimHandler = new MATSIM_simEventHandler(router);
							
							// add the map to the handler
							theSimHandler.setTheMap(ourMap);
							// add the agents to the handler
							theSimHandler.setTheAgents(masterAgents);
							// add the queue to pass agent ids from agentEvenHandler to simHandler
							theSimHandler.setAgentsToReplan(agentsToReplan);
							// Add the listener for Sim Step End 
							qSim.addQueueSimulationListeners(theSimHandler);
							
							qSim.addQueueSimulationListeners(travelTime);
							return qSim;
						}
					});
				}
			});
			
			// Everything is set up, let's run this thing
			controler.run();
			
			//System.out.println("Total travel time was: " + theAgentHandler.getTotalTravelTime());
			totalTravelTime = theAgentHandler.getTotalTravelTime();
		}
		
		catch ( Exception ee ) {
			Logger.getLogger("There was an exception: \n" + ee);
			
			// if one catches an exception, then one needs to explicitly fail the test:
			Assert.fail();
		}
		
		System.out.println("\n\nTotal travel time was: " + totalTravelTime + " seconds");
		
		Long stopTime = System.currentTimeMillis();
		
		Long timeToRun = (stopTime - startTime) / 1000;
		System.out.println("\n it took: " + timeToRun + " seconds to run this sim");
		
		System.out.println("\n\nWell, we got to the end. \n\n\n\n");	
	}
}