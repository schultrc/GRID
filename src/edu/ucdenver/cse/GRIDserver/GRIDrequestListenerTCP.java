package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteRequest;

public class GRIDrequestListenerTCP extends Thread {
	Socket theSocket = null;
	
	public GRIDrequestListenerTCP( Socket client){
		this.theSocket = client;
	}

	public void run() {
		System.out.println("Inside listener");
		
		try {
			ObjectInputStream  inputStream  = new ObjectInputStream (theSocket.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream (theSocket.getOutputStream());

			Object theRequest = inputStream.readObject();
			
			// Add more options as we define requests
			if (theRequest instanceof GRIDrouteRequest) {
				System.out.println("Route Request Received");

				GRIDroute theRoute = new GRIDroute();
				
				theRoute.addIntersection("DUMMY_INT");
				theRoute.addRoad("DUMMY_ROAD");

				System.out.println("Object to be written = " + theRoute.toString());
				outputStream.writeObject(theRoute);
				//outputStream.flush();
				
				inputStream.close();
				outputStream.close();
			}
			
			else {
				System.out.println("ERROR: Unknown Request Received");				
			}
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
