/**
 * 
 */
/**
 * @author Ray
 *
 */
package edu.ucdenver.cse.GRIDsim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
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

import edu.ucdenver.cse.GRIDclient.GRIDrequest;
import edu.ucdenver.cse.GRIDclient.GRIDrequestSender;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDmessages.GRIDServerTerm;
import edu.ucdenver.cse.GRIDutil.FileUtils;
import edu.ucdenver.cse.GRIDcommon.logWriter;

public class GRIDsim {
	
	private CommandLine theCmdLine;
	private Path outputDir;
	final ConcurrentHashMap<String, GRIDagent> masterAgents;
	final Queue<String> agentsToReplan;
	private int agentControlPercent;
	
	// The official map
	private GRIDmap ourMap;

	public static void main(String[] args) {
		GRIDsim theSim = new GRIDsim(args);
		
		theSim.simulate();
	}
	
	public GRIDsim(String[] theArgs) {
		GRIDsimCmdLine cmdLine = new GRIDsimCmdLine(theArgs);

		try {
			this.theCmdLine = cmdLine.parseArgs();
		} 
		
		catch (ParseException e) {
			System.out.println("This is bad: " + e.toString());
		}
		
		masterAgents = new ConcurrentHashMap<String, GRIDagent> ();
		
		// This will get filled and emptied via departure / arrival events
		agentsToReplan = new LinkedList<String>();
		
		ourMap = null;
		
		// Default control is 100%
		agentControlPercent = 100;
	}
	
	private void simulate() {
		
		/* This is the primary simulation. It will run the sim (currently matsim) making requests of the 
		 * GRIDserver as needed to determine routes
		 */		
		System.out.println("It's not hard--you just gotta use finesse!");
		
		if (!setPath()) {
			// This sucks, we can't even log it
			System.out.println("Error setting output path");
			System.exit(1);
		}
		
		// Get the logger. Since this "should" be the first time, we can set the path
		
		logWriter.setOutputDir(outputDir);
		logWriter.setLogPrefix("SIM");
		
		logWriter.log(Level.INFO, "DOES THIS WORK???!!!");		
				
		double totalTravelTime = 0;
				
		// Load our version of the map first
		GRIDmapReader masterMap = new GRIDmapReader();
		
		// the matsim config file
		String configFile;
		
		if (theCmdLine.hasOption("config")) {

			configFile = (String) theCmdLine.getOptionValue("config");
			System.out.println("The mapfile from the cmdLine: " + configFile);
		}
		
		else {
			configFile = FileUtils.getXmlFile();
		}
	        
	    if (configFile == "") {
	    	logWriter.log(Level.WARNING, "You didn't choose a config file!!!");
	    	System.exit(0);
	    }
	    
	    logWriter.log(Level.INFO, "CONFIG FILE: " + configFile + " in use\n\n\n");
		
	    if (theCmdLine.hasOption("AgtCtrl")) {
	    	this.agentControlPercent = Integer.parseInt( theCmdLine.getOptionValue("AgtCtrl"));
	    	
	    	if (this.agentControlPercent < 0 || this.agentControlPercent > 100) {
	    		logWriter.log(Level.WARNING, this.getClass().getName() + " Input error - agent control " +
	    	                                 "percentage out of range");
	    		System.exit(1);
	    	}
	    }
	    
	    logWriter.log(Level.INFO, "Agent Control: " + this.agentControlPercent + " in use\n\n\n");

	    
	    Long startTime = System.currentTimeMillis();
		logWriter.log(Level.INFO, "Starting SIM @" + startTime.toString());

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
			
			// build our map from the data file
			ourMap = masterMap.readMapFile(mapFile);
			ourMap.initMap();

			// From WithinDayReplanning
			Set<String> analyzedModes = new HashSet<String>();
			analyzedModes.add(TransportMode.car);
			final TravelTimeCollector travelTime = new TravelTimeCollector(controler.getScenario(), analyzedModes);
			controler.getEvents().addHandler(travelTime);
			// end WithinDayReplanning
			
			// Add our handler for Link Events			
			MATSIM_agentEventHandler theAgentHandler = new MATSIM_agentEventHandler(agentControlPercent);
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
	    logWriter.log(Level.INFO, "Total travel time was: " + totalTravelTime + " seconds");
		
		Long stopTime = System.currentTimeMillis();
		
		Long timeToRun = (stopTime - startTime) / 1000;
		System.out.println("\n it took: " + timeToRun + " seconds to run this sim");
		logWriter.log(Level.INFO, "it took: " + timeToRun + " seconds to run this sim");
		
		// contact the server for a new route
		GRIDrequestSender theRequestSender = new GRIDrequestSender();

		GRIDrequest testReq = new GRIDServerTerm();

		// Tell the server
		theRequestSender.sendRequest(testReq);

		System.out.println("\n\nWell, we got to the end. \n\n\n\n");	
	}
	
	public Boolean setPath() {
		// Get the output dir first, so log messages go there
		
		if (theCmdLine.hasOption("output")) {
			this.outputDir = Paths.get((String) theCmdLine.getOptionValue("output"));
		}

		else {
			this.outputDir = Paths.get("./TEST_RUNS/");
		}

		if ((Files.exists(this.outputDir, LinkOption.NOFOLLOW_LINKS))) {

			if (Files.isDirectory(this.outputDir)) {
				// Do we want to clear it at this point? Add a cmd line flag
			}
		}

		else {
			// We need to create a new directory
			System.out.println("Attempting to create: " + this.outputDir.toString());
			try {
				Files.createDirectories(this.outputDir);
			} 
			
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}		
		return true;
	}
}