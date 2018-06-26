package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;
import java.nio.file.LinkOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDmessages.GRIDServerTerm;
import edu.ucdenver.cse.GRIDmessages.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDmessages.GRIDtimeMsg;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import org.apache.commons.cli.CommandLine;

public class GRIDrequestListenerTCP extends Thread {
	Socket theSocket = null;
	private CommandLine theCmdLine;
	private Path outputDir;
	private GRIDworld theGRID = null;
	ObjectInputStream  inputStream;
	ObjectOutputStream outputStream;
	
	public GRIDrequestListenerTCP( Socket client, GRIDworld grid){
		this.theSocket = client;
		this.theGRID   = grid;
	}

	public void run() {		
		try {
			inputStream  = new ObjectInputStream (theSocket.getInputStream());
			outputStream = new ObjectOutputStream (theSocket.getOutputStream());

			Object theRequest = inputStream.readObject();

			if (theRequest instanceof GRIDrouteRequest) {

				// MFS log/console output for testing...
				logWriter.log(Level.INFO, "Route Request Received: " + (((GRIDrouteRequest)theRequest).toString()));
				//System.out.println("Route Request Received: " + (((GRIDrouteRequest)theRequest).toString()));

				// We need to know if we have to remove an old route or not
				boolean newAgentFlag = false;
				
				GRIDagent tempAgent;
				
				if (theGRID.getMasterAgents().containsKey(((GRIDrouteRequest) theRequest).getAgentID() )){
					//System.out.println("Agent: " + (((GRIDrouteRequest) theRequest).getAgentID()) + " already exists!");
					tempAgent = theGRID.getMasterAgents().get(((GRIDrouteRequest) theRequest).getAgentID());
					tempAgent.setLink(((GRIDrouteRequest) theRequest).getOrigin());
					
					// Agents can change destination (and origin?) - same agent requests new route
					// We should check before setting
					tempAgent.setOrigin(((GRIDrouteRequest) theRequest).getOrigin());
					tempAgent.setDestination(((GRIDrouteRequest) theRequest).getDestination());
				}
				
				else {
					//System.out.println("Agent: " + (((GRIDrouteRequest) theRequest).getAgentID()) + " NOT FOUND!");

					tempAgent = new GRIDagent((((GRIDrouteRequest) theRequest).getAgentID()), 
					                          (((GRIDrouteRequest) theRequest).getOrigin()), 
					                          (((GRIDrouteRequest) theRequest).getDestination()));
					
					this.theGRID.getMasterAgents().put(((GRIDrouteRequest) theRequest).getAgentID(), tempAgent);
					
					newAgentFlag = true;
				}

				logWriter.log(Level.INFO, "RequestListener - Agent to be replanned: " + tempAgent.toString() +
						                  " at time: " + theGRID.getTime());

				GRIDpathfinder theALG = new GRIDpathfinder(this.theGRID.getMap());
				
				theALG.init();
				
				GRIDroute tempRoute = theALG.findPath(tempAgent, theGRID.getTime());
				
				if (tempRoute == null) {
					//logWriter.log(Level.WARNING, "RequestListener - ROUTE WAS NULL for agent: " + tempAgent.getId());
					
					// We should do something here???
					inputStream.close();
					outputStream.close();

					return;
				}
				
				// RCS This is where we can convert what we get from the alg to a real route, if it isn't one
				//tempRoute.setRoads(this.theGRID.getMap().getPathByRoad(tempRoute.getIntersections()));
				logWriter.log(Level.INFO, "RequestListener - Route to be written = " +
				                           tempRoute.toString() +
				                           " at GRID time: " + 
				                           this.theGRID.getTime());

				// need to update the map with the new agent's route
				
				this.theGRID.getMap().updateMapWithAgents(tempRoute, this.theGRID.getTime());
				
				// If we just added this agent, there is no "existing route" to remove
				if(!newAgentFlag) {
					// This is wrong, as it will remove the route as of time now, not at the proper time
					// GRIDsegments will fix this
					this.theGRID.getMap().removeAgentsFromMap(tempAgent.getCurrentRoute(), this.theGRID.getTime());
				}
				
				tempAgent.setRoute(tempRoute);
				
				outputStream.writeObject(tempRoute);
				outputStream.flush();
				
				inputStream.close();
				outputStream.close();
			}
			
			else if (theRequest instanceof GRIDtimeMsg) {
				// Only log every so often
				if ((((GRIDtimeMsg) theRequest).getTheTime() % 1000) == 0) {
				logWriter.log(Level.INFO, "RequestListener - GridTimeMsg received with time: " +
						((GRIDtimeMsg) theRequest).getTheTime());

				}
				
				this.theGRID.setTime(((GRIDtimeMsg) theRequest).getTheTime());
				
				// need to reduce the map data
				for(GRIDroad road : this.theGRID.getMap().getRoads().values() ) {
					road.removeAgentsFromRoadAtTime(this.theGRID.getTime());
				}
				
				
				inputStream.close();
				outputStream.close();
			}
			
			else if (theRequest instanceof GRIDServerTerm) {
				// Someone wants the server to shutdown
				logWriter.log(Level.INFO, this.getClass().getName() + " TERM message recieved - " +
				                          "shutting down at time: " + this.theGRID.getTime());
				
				System.exit(0);
			}
			
			else {
				logWriter.log(Level.WARNING, "RequestListener - ERROR: Unknown Request Received");			
			}
		} 
		catch (SocketException se) {
			// se.printStackTrace();
			System.out.println("Socket Exception");
			// try to keep going
			//inputStream.close();
			//outputStream.close();
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// move to utility
	public Boolean setPath() {
		// Get the output dir first, so log messages go there

		if (theCmdLine.hasOption("output")) {
			outputDir = Paths.get((String) theCmdLine.getOptionValue("output"));
		}

		else {
			outputDir = Paths.get("./TEST_RUNSes/");
		}

		if ((Files.exists(outputDir, LinkOption.NOFOLLOW_LINKS))) {

			if (Files.isDirectory(outputDir)) {
				// Do we want to clear it at this point? Add a cmd line flag
			}
		}

		else {
			// We need to create a new directory
			System.out.println("Attempting to create: " + outputDir.toString());
			try {
				Files.createDirectories(outputDir);
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
