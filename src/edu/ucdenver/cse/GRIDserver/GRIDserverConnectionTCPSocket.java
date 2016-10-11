package edu.ucdenver.cse.GRIDserver;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import edu.ucdenver.cse.GRIDmap.*;

public final class GRIDserverConnectionTCPSocket implements GRIDserverConnection {

	private ServerSocket servSocket = null;
	private final int monitorPort = 9998;
    
	private GRIDworld theGRID;
	
	public GRIDserverConnectionTCPSocket(GRIDworld grid) {
		this.theGRID = grid;
	}
	
	public void run() {

		try {
			// Make the port configurable
			servSocket = new ServerSocket(monitorPort);
			while (true) {

				Socket clientSocket = servSocket.accept();

				//System.out.println("Client has connected");

				GRIDrequestListenerTCP listener = new GRIDrequestListenerTCP(clientSocket, theGRID);
				listener.run();
			}

		} catch (SocketException se) {
			se.printStackTrace();
			// System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
