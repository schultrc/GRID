package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDmap.GRIDroad;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;

import java.util.ArrayList;
import java.util.logging.Level;

import org.junit.Test;

public class GRIDtestRunner{

    private GRIDmapReader myReader = new GRIDmapReader();
    // SmallNetwork2 PuebloNetwork 5x5network RyeNetwork
    private GRIDmap myMap = myReader.readMapFile("data/5x5network.xml");
    //private GRIDmap networkMap = graphMiddleware(myMap);
    
    private GRIDagent testAgent001 = getTestAgent();
    //private GRIDintersection from = new GRIDintersection("test",1d,2d);
    //private GRIDintersection to = new GRIDintersection("test",1d,2d);

    @Test
    public void runTest()
    { // Pueblo start to finish 34.97s



        System.out.println("this is another nother test");

	    System.out.println("\nStarting test. . .");
        // Pueblo start to finish 34.97s
        // from.setId("1040921516"); // from.setId("01"); // 1040921516 // 2
        // to.setId("864162469");   // to.setId("10"); // 864162469 // 50
    	Long startTime = System.nanoTime();

        //GRIDheapAlg greedy = new GRIDheapAlg();
    	GRIDpathfinder theALG = new GRIDpathfinder(myMap);

        //GRIDheapDynamicAlg dyna = new GRIDheapDynamicAlg(myMap); //
        //myPathGreedy = greedy.shortestPath(networkMap,"1040921516","864162469");

        //GRIDselfishAlg test001 = new GRIDselfishAlg(testAgent001, networkMap, 0L); // GRIDpathrecalc GRIDselfishAlg
        //GRIDpathrecalc test001 = new GRIDpathrecalc(testAgent001, networkMap, 0L); // GRIDpathrecalc GRIDselfishAlg
        GRIDroute outRoute = new GRIDroute();
        /*GRIDpathrecalc test001 = new GRIDpathrecalc(testAgent001, networkMap, 0L); // GRIDpathrecalc GRIDselfishAlg
        outRoute = test001.findPath();*/
        myMap.initMap();
        theALG.init();
        outRoute = theALG.findPath(testAgent001, 0L); // 0L

        //logWriter.log(Level.INFO, "Route is: " + outRoute.toString());
        //MFS remove following console output...
        System.out.println(outRoute.toString());

        // This method does not currently do anything, i.e., returns 0
        System.out.println("\n\nCalculated Travel Time: " + outRoute.getcalculatedTravelTime());

        long stopTime = System.nanoTime();
        long timeToRun = ((stopTime - startTime)/1000000);
        
        System.out.print("\nTook " + timeToRun/1000.0 + " Seconds");
        System.out.print("\n\nAnd we're done.\n");
    }

    private GRIDagent getTestAgent()
    { // String Id, String newLink, String origin, String destination
        String agtID = "testAgent001",
                currentLink = "1to2", // 40963664_0 106292026_0
                currentIntrx = "not a real value", // 1040921516 // 2
                destination = "24to25";    // 72823276_0 this is a link--see below...
        // 864162469 - 1400447055 99282649_0_r [72823276_0 problem link] 17133393_0_r

        GRIDagent myAgent = new GRIDagent(agtID,currentLink,currentIntrx,destination, false, false);

        return myAgent;
    }
}