package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDmap.*;
import edu.ucdenver.cse.GRIDuser.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;

public class GRIDrequestListenerTCP extends Thread {
	Socket theSocket = null;
	private GRIDworld theGRID = null;
	ObjectInputStream  inputStream;
	ObjectOutputStream outputStream;
	
	public GRIDrequestListenerTCP( Socket client, GRIDworld grid){
		this.theSocket = client;
		this.theGRID    = grid;
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
				
				GRIDagent tempAgent;
				
				// Is this a new agent or an existing one?
				
				
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
				}

				//System.out.println("Agent to be replanned: " + tempAgent.toString());

				GRIDheapDynamicAlg theALG = new GRIDheapDynamicAlg(theGRID.getTheMap());
				GRIDroute tempRoute = theALG.findPath(tempAgent, timeNow);
				
				tempRoute.setRoads(theGRID.getTheMap().getPathByRoad(tempRoute.getIntersections()));
				
				// need to update the map with the new agent's route
				
				//System.out.println("Object to be written = " + tempRoute.toString());
				outputStream.writeObject(tempRoute);
				outputStream.flush();
				
				inputStream.close();
				outputStream.close();
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
