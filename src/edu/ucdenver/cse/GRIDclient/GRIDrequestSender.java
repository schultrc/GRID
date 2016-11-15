package edu.ucdenver.cse.GRIDclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmessages.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDmessages.GRIDtimeMsg;

public class GRIDrequestSender {

	private Socket socket = null;
	private ObjectInputStream inputStream = null;
	private ObjectOutputStream outputStream = null;
	private Object theReturnObj;

	public Object sendRequest(GRIDrequest theRequest) {
		
		try {

			// Make this configurable
			socket = new Socket("localHost", 9998);
			logWriter.log(Level.INFO, "requestSender - Connected");

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

			logWriter.log(Level.FINEST, "requestSender - Sending: " + theRequest.toString());
			// Send the route request
			outputStream.writeObject(theRequest);
			outputStream.flush();

			//System.out.println("request sent");

			if (theRequest instanceof GRIDrouteRequest) {
				// Get the new route back
				theReturnObj = inputStream.readObject();
				
				if(theReturnObj == null) {
					logWriter.log(Level.WARNING, "requestSender - Found null route");
					
				}
				return theReturnObj;
				//System.out.println("The new route is: " + ((GRIDroute) theReturnObj).toString());
			}

			// handle other request types here
			
			if (theRequest instanceof GRIDtimeMsg) {
				// we don't expect anything back
				return null;
			}
			inputStream.close();
			outputStream.close();

			socket.close();

		} catch (SocketException se) {
			// se.printStackTrace();
			logWriter.log(Level.WARNING,"requestSender - Socket Exception");
			
			//System.exit(1);

			// System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			//System.exit(1);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return null;
	}
}
