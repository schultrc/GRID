package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDmessages.GRIDServerTerm;
import edu.ucdenver.cse.GRIDmessages.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDmessages.GRIDtimeMsg;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;

public class GRIDrequestListenerTCP extends Thread {
	Socket theSocket = null;
	private GRIDworld theGRID = null;
	ObjectInputStream  inputStream;
	ObjectOutputStream outputStream;
	String weightType;
	boolean weAreTiming;
	
	public GRIDrequestListenerTCP( Socket client, GRIDworld grid, String weightType){
		this.theSocket = client;
		this.theGRID   = grid;
		this.weightType = weightType;
		
		// change this value to time various methods
		weAreTiming = true;
	}

	public void run() {		
		try {
			inputStream  = new ObjectInputStream (theSocket.getInputStream());
			outputStream = new ObjectOutputStream (theSocket.getOutputStream());

			Object theRequest = inputStream.readObject();
			
			if (theRequest instanceof GRIDrouteRequest) {
				//System.out.println("Route Request Received: " + (((GRIDrouteRequest)theRequest).toString()));
				
				// We need to know if we have to remove an old route or not
				boolean newRouteFlag = false;
				
				GRIDagent tempAgent;
				
				if (theGRID.getMasterAgents().containsKey(((GRIDrouteRequest) theRequest).getAgentID() )){

					tempAgent = theGRID.getMasterAgents().get(((GRIDrouteRequest) theRequest).getAgentID());
					tempAgent.setLink(((GRIDrouteRequest) theRequest).getOrigin());
					
					// Agents can change destination (and origin?) - same agent requests new route
					// We should check before setting
					tempAgent.setOrigin(((GRIDrouteRequest) theRequest).getOrigin());
					tempAgent.setDestination(((GRIDrouteRequest) theRequest).getDestination());
				}
				
				else {
					tempAgent = new GRIDagent((((GRIDrouteRequest) theRequest).getAgentID()), 
					                          (((GRIDrouteRequest) theRequest).getOrigin()), 
					                          (((GRIDrouteRequest) theRequest).getDestination()));
					
					this.theGRID.getMasterAgents().put(((GRIDrouteRequest) theRequest).getAgentID(), tempAgent);
					
					newRouteFlag = true;
				}

				logWriter.log(Level.INFO, "RequestListener - Agent to be replanned: " + tempAgent.toString() +
						                  " at time: " + theGRID.getTime());

				GRIDpathfinder theALG = new GRIDpathfinder(this.theGRID.getMap(), this.weightType);
				
				theALG.init();
				GRIDroute tempRoute;
				
				if (weAreTiming) {
					long startTime = System.nanoTime();
					tempRoute = theALG.findPath(tempAgent, theGRID.getTime());
					long endTime = System.nanoTime();

					long duration = (endTime - startTime);
					
					logWriter.log(Level.INFO, "Calculating the route took: " + duration + " nanoseconds");
				}
				else {
					tempRoute = theALG.findPath(tempAgent, theGRID.getTime());
				}
				
				if (tempRoute == null) {
					logWriter.log(Level.WARNING, "RequestListener - ROUTE WAS NULL for agent: " + tempAgent.getId());
					
					// We should do something here???
					inputStream.close();
					outputStream.close();

					return;
				}
				
				logWriter.log(Level.INFO, "RequestListener - Route to be written = " + 
				                           tempRoute.toString() +
				                           " at GRID time: " + 
				                           this.theGRID.getTime());

				// need to update the map with the new agent's route
				if (weAreTiming) {
					long startTime = System.nanoTime();
					this.theGRID.getMap().updateMapWithAgents(tempRoute);
					long endTime = System.nanoTime();

					long duration = (endTime - startTime);
					
					logWriter.log(Level.INFO, "Adding agents to the route took: " + duration + " nanoseconds");
				}
				else {
					this.theGRID.getMap().updateMapWithAgents(tempRoute);
				}
				
				// If we just added this agent, there is no "existing route" to remove
				if(!newRouteFlag) {
					// This is wrong, as it will remove the route as of time now, not at the proper time
					// GRIDsegments will fix this
					if (weAreTiming) {
						long startTime = System.nanoTime();
						this.theGRID.getMap().removeAgentsFromMap(tempAgent.getCurrentRoute(), this.theGRID.getTime());
						long endTime = System.nanoTime();

						long duration = (endTime - startTime);
						
						logWriter.log(Level.INFO, "removing agents from the map took: " + duration + " nanoseconds");
					}
					else {
						this.theGRID.getMap().removeAgentsFromMap(tempAgent.getCurrentRoute(), this.theGRID.getTime());
					}
					
				}
				
				tempAgent.setRoute(tempRoute);
				
				outputStream.writeObject(tempRoute);
				outputStream.flush();
				
				inputStream.close();
				outputStream.close();
			}
			
			else if (theRequest instanceof GRIDtimeMsg) {
				this.theGRID.setTime(((GRIDtimeMsg) theRequest).getTheTime());
				
				// need to remove the values in the hashMap
				if ((((GRIDtimeMsg) theRequest).getTheTime() % 1000) == 0) {
					for(GRIDroad road : this.theGRID.getMap().getRoads().values() ) {
						road.removeAgentsFromRoadAtTime(this.theGRID.getTime());
					}
					// Only log every so often
					logWriter.log(Level.INFO, "RequestListener - GridTimeMsg received with time: " +
							((GRIDtimeMsg) theRequest).getTheTime());
				}
				
				inputStream.close();
				outputStream.close();
			}
			
			else if (theRequest instanceof GRIDServerTerm) {
				// Someone wants the server to shutdown
				logWriter.log(Level.INFO, this.getClass().getName() + " TERM message recieved - " +
				                          "shutting down at time: " + this.theGRID.getTime());
				
				inputStream.close();
				outputStream.close();
				System.exit(0);
			}
			
			else {
				logWriter.log(Level.WARNING, "RequestListener - ERROR: Unknown Request Received");			
			}
		} 
		catch (SocketException se) {
			// se.printStackTrace();
			System.out.println("Socket Exception");
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
