package edu.ucdenver.cse.GRIDserver;

import java.util.logging.Level;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDutil.FileUtils;
import org.apache.commons.cli.CommandLine;

public class GRIDserver extends Thread {

	public static void main(String[] args) {
	
		System.out.println("It's not hard--you just gotta use finesse!");

		GRIDserverCmdLine cmdLine = new GRIDserverCmdLine(args);
		CommandLine theCmdLine = cmdLine.parseArgs();
		
		String mapFile = "";
		
		if(theCmdLine.hasOption("mf")) {
			mapFile = theCmdLine.getOptionValue("mf");
		}
		else {
			mapFile = FileUtils.getXmlFile();			
		}
		
		// Load our version of the map first
		GRIDmapReader masterMap = new GRIDmapReader();

	    if (mapFile == "") {
	    	logWriter.log(Level.WARNING, "You didn't choose a map file!!!");
	    	System.exit(0);
	    }
	    
	    // The official map
		GRIDmap ourMap = masterMap.readMapFile(mapFile);
	    
	    logWriter.log(Level.INFO, "CONFIG FILE: " + mapFile + " in use\n\n\n");
	    
	    GRIDworld theGRID = new GRIDworld(ourMap);
	    
		// Setup the connection for the clients.
		GRIDserverConnection myConnection;
		myConnection = new GRIDserverConnectionTCPSocket(theGRID);
		myConnection.run();
	}
}
