package edu.ucdenver.cse.GRIDserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public final class GRIDserverConnectionTCPSocket implements GRIDserverConnection {

	private ServerSocket servSocket = null;
	private final int monitorPort = 9998;
	private String weightType;
    
	private GRIDworld theGRID;
	
	public GRIDserverConnectionTCPSocket(GRIDworld grid, String weightType) {
		this.theGRID = grid;
		this.weightType = weightType;
	}
	
	public void run() {

		try {
			// Make the port configurable
			servSocket = new ServerSocket(monitorPort);
			while (true) {

				Socket clientSocket = servSocket.accept();
				
				//System.out.println("Client has connected");
				GRIDrequestListenerTCP listener = new GRIDrequestListenerTCP(clientSocket, theGRID, weightType );
				listener.run();
			}

		} catch (SocketException se) {
			se.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
