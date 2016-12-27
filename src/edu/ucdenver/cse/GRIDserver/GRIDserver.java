package edu.ucdenver.cse.GRIDserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import edu.ucdenver.cse.GRIDcommon.logWriter;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDutil.FileUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class GRIDserver extends Thread {

	private CommandLine theCmdLine;
	private Path outputDir;
	GRIDworld theGRID;

	public static void main(String[] args) {
		GRIDserver theServer = new GRIDserver(args);
		theServer.init();

		theServer.serve();
		
		logWriter.stop();
	}

	public GRIDserver(String[] theArgs) {
		GRIDserverCmdLine cmdLine = new GRIDserverCmdLine(theArgs);

		try {
			this.theCmdLine = cmdLine.parseArgs();
		}

		catch (ParseException e) {
			System.out.println("This is bad: " + e.toString());
		}
	}

	private void init() {

		System.out.println("It's not hard--you just gotta use finesse!");
		
		if (!setPath()) {
			// This sucks, we can't even log it
			System.out.println("Error setting output path");
			System.exit(1);
		}
		logWriter.setOutputDir(outputDir);

		String mapFile = "";

		if (theCmdLine.hasOption("map")) {
			mapFile = theCmdLine.getOptionValue("map");
		} else {
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
		
		if (theCmdLine.hasOption("sim")) {
			theGRID = new GRIDworld(ourMap, 0L);
		}

		else {
			theGRID = new GRIDworld(ourMap, (System.currentTimeMillis()/1000) );
		}
	}

	private void serve() {

		// Setup the connection for the clients.
		GRIDserverConnection myConnection;
		myConnection = new GRIDserverConnectionTCPSocket(theGRID);
		myConnection.run();
	}

	// move to utility
	public Boolean setPath() {
		// Get the output dir first, so log messages go there

		if (theCmdLine.hasOption("output")) {
			outputDir = Paths.get((String) theCmdLine.getOptionValue("output"));
		}

		else {
			outputDir = Paths.get("../TEST_RUNS/output");
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
}
