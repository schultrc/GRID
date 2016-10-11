package edu.ucdenver.cse.GRIDuser;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import edu.ucdenver.cse.GRIDcommon.*;

public class GRIDclientSocket extends Thread {

	private Socket socket = null;
	private ObjectInputStream inputStream = null;
	private ObjectOutputStream outputStream = null;
	private GRIDrouteRequest myRequest;
	private GRIDroute myRoute;

	public GRIDclientSocket() {

	}

	public void communicate() {

		myRequest = new GRIDrouteRequest("TEST_ID", "1to2", "24to25");

		while (true) {
			try {

				// Make this configurable
				socket = new Socket("localHost", 9998);
				System.out.println("Connected");
				
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());

				System.out.println("Sending: " + myRequest.toString());
				// Send the route request
				outputStream.writeObject(myRequest);
				outputStream.flush();
				
				System.out.println("request sent");

				// Get the new route back
				myRoute = (GRIDroute) inputStream.readObject();

				System.out.println("The new route is: " + myRoute.toString());

				inputStream.close();
				outputStream.close();

				socket.close();

			} catch (SocketException se) {
				// se.printStackTrace();
				System.out.println("Socket Exception");
				System.exit(1);

				// System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// For test purposes only
	public static void main(String[] args) {
		GRIDclientSocket testSocket = new GRIDclientSocket();
		testSocket.communicate();
	}
}
