package edu.ucdenver.cse.GRIDuser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import edu.ucdenver.cse.GRIDcommon.*;
import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDutil.FileUtils;

public class GRIDclientTest extends Thread {

	private ObjectInputStream inputStream = null;
	private GRIDroute myRoute;
	private GRIDmap theMap;

	public GRIDclientTest() {
		// Load our version of the map first
		GRIDmapReader masterMap = new GRIDmapReader();

		String mapFile = FileUtils.getXmlFile();

		if (mapFile == "") {
			logWriter.log(Level.WARNING, "You didn't choose a map file!!!");
			System.exit(0);
		}

		// The official map
		theMap = masterMap.readMapFile(mapFile);

		//logWriter.log(Level.INFO, "CONFIG FILE: " + mapFile + " in use\n\n\n");
	}

	public void communicate() {
		Long startTime = System.currentTimeMillis();
		
		int i =0;
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
		GRIDclientTest testSocket = new GRIDclientTest();
		testSocket.communicate();
	}
}
