package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDmessages.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDmessages.GRIDtimeMsg;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;

public class GRIDrequestListenerTCP extends Thread {
	Socket theSocket = null;
	private GRIDworld theGRID = null;
	ObjectInputStream  inputStream;
	ObjectOutputStream outputStream;
	
	public GRIDrequestListenerTCP( Socket client, GRIDworld grid){
		this.theSocket = client;
		this.theGRID   = grid;
	}

	public void run() {
		//System.out.println("Inside listener");
		
		long timeNow = System.currentTimeMillis() / 1000;
		try {
			inputStream  = new ObjectInputStream (theSocket.getInputStream());
			outputStream = new ObjectOutputStream (theSocket.getOutputStream());

			Object theRequest = inputStream.readObject();
			
			// Add more options as we define requests
			if (theRequest instanceof GRIDrouteRequest) {
				//System.out.println("Route Request Received: " + (((GRIDrouteRequest)theRequest).toString()));
				
				// We need to know if we have to remove an old route or not
				boolean newAgentFlag = false;
				
				GRIDagent tempAgent;

				// This is broken, need to be able to change location / destination
				
				if (theGRID.getMasterAgents().containsKey(((GRIDrouteRequest) theRequest).getAgentID() )){
					//System.out.println("Agent: " + (((GRIDrouteRequest) theRequest).getAgentID()) + " already exists!");
					tempAgent = theGRID.getMasterAgents().get(((GRIDrouteRequest) theRequest).getAgentID());
				}
				
				else {
					//System.out.println("Agent: " + (((GRIDrouteRequest) theRequest).getAgentID()) + " NOT FOUND!");

					tempAgent = new GRIDagent((((GRIDrouteRequest) theRequest).getAgentID()), 
					                          (((GRIDrouteRequest) theRequest).getOrigin()), 
					                          (((GRIDrouteRequest) theRequest).getDestination()));
					
					theGRID.getMasterAgents().put(((GRIDrouteRequest) theRequest).getAgentID(), tempAgent);
					
					newAgentFlag = true;
				}

				System.out.println("Agent to be replanned: " + tempAgent.toString() );

				GRIDheapDynamicAlg theALG = new GRIDheapDynamicAlg(theGRID.getMap());
				GRIDroute tempRoute = theALG.findPath(tempAgent, timeNow);
				
				if (tempRoute == null) {
					System.out.println("ROUTE WAS NULL:");
					inputStream.close();
					outputStream.close();

					return;
				}
				
				tempRoute.setRoads(theGRID.getMap().getPathByRoad(tempRoute.getIntersections()));
				System.out.println("Route to be written = " + tempRoute.toString() +"\n\n");
				// need to update the map with the new agent's route
				
				
				theGRID.getMap().updateRoadsWithAgents(tempRoute, theGRID.getTime());
				
				if(!newAgentFlag) {
					// This is wrong, as it will remove the route as of now, not at the proper time
					// GRIDsegments will fix this
					theGRID.getMap().reduceRoadsWithAgents(tempAgent.getRoute(), theGRID.getTime());
				}
				
				tempAgent.setRoute(tempRoute);
				
				outputStream.writeObject(tempRoute);
				outputStream.flush();
				
				inputStream.close();
				outputStream.close();
			}
			
			else if (theRequest instanceof GRIDtimeMsg) {
				System.out.println("******* \n GridTimeMsg received with time: " + 
						((GRIDtimeMsg) theRequest).getTheTime() + "\n********");
				
				this.theGRID.setTime(((GRIDtimeMsg) theRequest).getTheTime());
			}
			
			else {
				System.out.println("ERROR: Unknown Request Received");				
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
}
