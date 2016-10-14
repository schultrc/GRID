package edu.ucdenver.cse.GRIDclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;

public class GRIDrequestSender {

	private Socket socket = null;
	private ObjectInputStream inputStream = null;
	private ObjectOutputStream outputStream = null;
	private Object theReturnObj;

	public Object sendRequest(GRIDrequest theRequest) {
		
		try {

			// Make this configurable
			socket = new Socket("localHost", 9998);
			System.out.println("Connected");

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

			System.out.println("Sending: " + theRequest.toString());
			// Send the route request
			outputStream.writeObject(theRequest);
			outputStream.flush();

			System.out.println("request sent");

			if (theRequest instanceof GRIDrouteRequest) {
				// Get the new route back
				theReturnObj = inputStream.readObject();
				//System.out.println("The new route is: " + ((GRIDroute) theReturnObj).toString());
			}

			// handle other request types here
			
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
			System.exit(1);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return theReturnObj;
	}
}
