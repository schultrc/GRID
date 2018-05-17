package edu.ucdenver.cse.GRIDclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import edu.ucdenver.cse.GRIDcommon.*;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDmessages.GRIDrouteRequest;
import edu.ucdenver.cse.GRIDmessages.GRIDtimeMsg;
import edu.ucdenver.cse.GRIDutil.FileUtils;

public class GRIDclientTest extends Thread {

	private GRIDmap theMap;
	private CommandLine theCmdLine;
	private Path outputDir;
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

		if (!setPath()) {
			// This sucks, we can't even log it
			System.out.println("Error setting output path");
			System.exit(1);
		}
		
		// Get the logger. Since this "should" be the first time, we can set the path

		//logWriter.setOutputDir(outputDir);
		//logWriter.log(Level.INFO, "DOES THIS WORK???!!!");

		
		if (theCmdLine.hasOption("map")) {
			System.out.print("We found the mapfile?");

			ourMapFile = (String) theCmdLine.getOptionValue("map");
			System.out.println("The mapfile from the cmdLine: " + ourMapFile);
		}

		else {
			ourMapFile = FileUtils.getXmlFile();
		}

		if (ourMapFile == "") {
			//logWriter.log(Level.WARNING, "You didn't choose a map file!!!");
			System.exit(0);
		}

		System.out.println("The map is: " + ourMapFile);

		// The official map
		GRIDmapReader masterMap = new GRIDmapReader();
		theMap = masterMap.readMapFile(ourMapFile);

		// logWriter.log(Level.INFO, "CONFIG FILE: " + mapFile + " in
		// use\n\n\n");

		long startTime = System.currentTimeMillis();

		int i = 0;

		int maxTries;

		if (theCmdLine.hasOption("a")) {
			maxTries = Integer.parseInt(theCmdLine.getOptionValue("a"));
		}

		else {
			maxTries = 10;
		}

		while (i < maxTries) {
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

			GRIDroute theRoute = (GRIDroute) theRequestSender.sendRequest(testReq);

			 System.out.println("The returned route was: " +
			 theRoute.toString());
			
			if (i%2 == 0) {
				System.out.println("sending time msg");
				GRIDtimeMsg timeMsg = new GRIDtimeMsg(Long.valueOf(i));
			}
			
			System.out.println("Try #: " + i);
			i++;
		}

		long stopTime = System.currentTimeMillis();

		long timeToRun = (stopTime - startTime) / 1000;
		System.out.println("\n it took: " + timeToRun + " seconds to run this sim");
	}

	// move to utility
	public Boolean setPath() {
		// Get the output dir first, so log messages go there
		
		if (theCmdLine.hasOption("output")) {
			outputDir = Paths.get((String) theCmdLine.getOptionValue("output"));
		}

		else {
			outputDir = Paths.get("./output");
		}

		if ((Files.exists(outputDir, LinkOption.NOFOLLOW_LINKS))) {

			if (Files.isDirectory(outputDir)) {
				// Do we want to clear it at this point? Add a cmd line flag
			}
		}

		else {
			// We need to create a new directory
			System.out.println("Attempting to create: " + outputDir.toString());
			try {
				Files.createDirectories(outputDir);
			} 
			
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}		
		return true;
	}

	public static void main(String[] args) {
		GRIDclientTest testSocket = new GRIDclientTest(args);

		testSocket.communicate();
	}
}
