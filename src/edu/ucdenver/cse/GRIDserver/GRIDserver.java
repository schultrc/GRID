package edu.ucdenver.cse.GRIDserver;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import org.apache.commons.cli.*;

import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDutil.FileUtils;


public class GRIDserver extends Thread {

	
	
	

	public static void main(String[] args) {
	
		System.out.println("It's not hard--you just gotta use finesse!");

		// The official map
		final GRIDmap ourMap;

		// Load our version of the map first
		GRIDmapReader masterMap = new GRIDmapReader();

		String mapFile = FileUtils.getXmlFile();
        
	    if (mapFile == "") {
	    	logWriter.log(Level.WARNING, "You didn't choose a map file!!!");
	    	System.exit(0);
	    }
	    
	    ourMap = masterMap.readMapFile(mapFile);
	    
	    logWriter.log(Level.INFO, "CONFIG FILE: " + mapFile + " in use\n\n\n");
	    
	    
		// Setup the connection for the clients.
		GRIDserverConnection myConnection;
		myConnection = new GRIDserverConnectionTCPSocket();
		myConnection.monitor();
	}
}
