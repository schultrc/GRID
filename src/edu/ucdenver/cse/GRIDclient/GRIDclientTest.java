package edu.ucdenver.cse.GRIDclient;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import edu.ucdenver.cse.GRIDcommon.*;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDutil.FileUtils;

public class GRIDclientTest extends Thread {

	private GRIDmap theMap;
	private CommandLine theCmdLine;
	private String ourMapFile;

	public GRIDclientTest(String[] theArgs) {
		GRIDclientCmdLine cmdLine = new GRIDclientCmdLine(theArgs);
		
		try {
			this.theCmdLine = cmdLine.parseArgs();
		}
		catch (ParseException e) {
			System.out.println("This is bad: " + e.toString());
		}		
	}

	public void communicate() {
		Long startTime = System.currentTimeMillis();

		if (theCmdLine.hasOption("m")) {
			System.out.print("We found the mapfile?");
			
			ourMapFile = (String)theCmdLine.getOptionValue("m");
			System.out.println("The mapfile from the cmdLine: " + ourMapFile);
		} else {
			ourMapFile = FileUtils.getXmlFile();
		}

		if (ourMapFile == "") {
			logWriter.log(Level.WARNING, "You didn't choose a map file!!!");
			System.exit(0);
		}

		System.out.println("The map is: " + ourMapFile);
		
		// The official map
		GRIDmapReader masterMap = new GRIDmapReader();
		theMap = masterMap.readMapFile(ourMapFile);

		// logWriter.log(Level.INFO, "CONFIG FILE: " + mapFile + " in
		// use\n\n\n");

		int i = 0;
		while (i < 10000) {
			Random rnd_home_node = new Random();
			Random rnd_work_node = new Random();

			int a = rnd_home_node.nextInt(theMap.getRoads().size());
			int b = rnd_work_node.nextInt(theMap.getRoads().size());
			
			ArrayList<String> roads = new ArrayList<String>();
			roads.addAll(theMap.getRoads().keySet());
			
			String source = theMap.getRoad(roads.get(a)).getId();
			String dest = theMap.getRoad(roads.get(b)).getId();
			
			GRIDrequestSender theRequestSender = new GRIDrequestSender();			
			GRIDrequest testReq = new GRIDrouteRequest("TheID" + i, source, dest);
			
			GRIDroute theRoute = (GRIDroute)theRequestSender.sendRequest(testReq);
			
			//System.out.println("The returned route was: " + theRoute.toString());
			System.out.println("Try #: " + i);
			i++;
		}
		
        Long stopTime = System.currentTimeMillis();
		
		Long timeToRun = (stopTime - startTime) / 1000;
		System.out.println("\n it took: " + timeToRun + " seconds to run this sim");
			
	}

	// For test purposes only
	public static void main(String[] args) {
		GRIDclientTest testSocket = new GRIDclientTest(args);

		testSocket.communicate();
	}
}
