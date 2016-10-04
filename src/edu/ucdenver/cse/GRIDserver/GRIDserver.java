package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteRequest;

public class GRIDserver extends Thread {

	private ServerSocket servSocket = null;
	private ObjectInputStream inputStream = null;
	private ObjectOutputStream outputStream = null;
	private boolean isConnected = false;

	public GRIDserver() {

	}

	public void communicate() {

		int count = 0;
		
		while (true) {
			try {
				// Make the port configurable
				servSocket = new ServerSocket(10007);
				Socket clientSocket = servSocket.accept();

				System.out.println("Client has connected");

				isConnected = true;
				outputStream = new ObjectOutputStream(clientSocket.getOutputStream());		
				inputStream = new ObjectInputStream(clientSocket.getInputStream());
				
				GRIDrouteRequest theRequest = (GRIDrouteRequest) inputStream.readObject();
				System.out.println("Object received = " + theRequest);
								
				GRIDroute theRoute = new GRIDroute();
				
				theRoute.addIntersection("DUMMY_INT");
				theRoute.addRoad("DUMMY_ROAD");

				System.out.println("Object to be written = " + theRoute.toString());
				outputStream.writeObject(theRoute);
				
				inputStream.close();
				outputStream.close();
				
				clientSocket.close();
				servSocket.close();
				
				count++;
				System.out.println("iteration: " + count);
				
			} catch (SocketException se) {
				se.printStackTrace();
				// System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException CNF) {
				System.out.println("Unable to read route request");
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) {
		GRIDserver routeRequestServer = new GRIDserver();
		routeRequestServer.communicate();
	}
}

