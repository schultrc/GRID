package edu.ucdenver.cse.GRIDserver;

import edu.ucdenver.cse.GRIDmap.GRIDmap;
import edu.ucdenver.cse.GRIDmap.GRIDmapReader;
import edu.ucdenver.cse.GRIDmap.GRIDroad;
import edu.ucdenver.cse.GRIDcommon.GRIDagent;
import edu.ucdenver.cse.GRIDcommon.GRIDroute;
import edu.ucdenver.cse.GRIDcommon.logWriter;

import java.util.ArrayList;
import java.util.logging.Level;

import org.junit.Test;

public class GRIDtestRunner{

    static logWriter testLW;

    private GRIDmapReader myReader = new GRIDmapReader();
    // SmallNetwork2 PuebloNetwork 5x5network RyeNetwork 5x5network_12cap
    private GRIDmap myMap = new GRIDmap();//myReader.readMapFile("data/5x5network_12cap.xml");
    //private GRIDmap networkMap = graphMiddleware(myMap);

    private GRIDagent testAgent001;// = getTestAgent();
    //private GRIDintersection from = new GRIDintersection("test",1d,2d);
    //private GRIDintersection to = new GRIDintersection("test",1d,2d);

    @Test
    public void runTest()
    { // Pueblo start to finish 34.97s


        this.myMap = myReader.readMapFile("data/AlamosaNetwork.xml");
        myMap.setupMapAsServer();
        this.testAgent001 = getTestAgent();

        //System.out.println("Intersections2: "+myMap.getIntersections());

        testLW.log(Level.INFO, "this is another nother test");

        System.out.println("\nStarting test. . .");
        // Pueblo start to finish 34.97s
        // from.setId("1040921516"); // from.setId("01"); // 1040921516 // 2
        // to.setId("864162469");   // to.setId("10"); // 864162469 // 50
        Long startTime = System.nanoTime();

        //GRIDheapAlg greedy = new GRIDheapAlg();
        GRIDpathfinder theALG = new GRIDpathfinder(myMap, "BPR");
        //GRIDheapDynamicAlg dyna = new GRIDheapDynamicAlg(myMap); //
        //myPathGreedy = greedy.shortestPath(networkMap,"1040921516","864162469");

        //GRIDselfishAlg test001 = new GRIDselfishAlg(testAgent001, networkMap, 0L); // GRIDpathrecalc GRIDselfishAlg
        //GRIDpathrecalc test001 = new GRIDpathrecalc(testAgent001, networkMap, 0L); // GRIDpathrecalc GRIDselfishAlg
        //GRIDroute outRoute = new GRIDroute();
        /*GRIDpathrecalc test001 = new GRIDpathrecalc(testAgent001, networkMap, 0L); // GRIDpathrecalc GRIDselfishAlg
        outRoute = test001.findPath();*/
        GRIDroute outRoute = theALG.findPath(testAgent001, 0L);

        //ListIterator<String> pathIterator = outRoute.Intersections.listIterator();

        /*assertNotNull(myMap);
        assertNotNull(outRoute);
        assertTrue(outRoute.Intersections.size() > 0);*/

        //System.out.println("\nShortest path: "+myPathGreedy);
        //System.out.println("\nShortest path: "+myPathDynamic);

        //System.out.print("\nPath:\n");
        //for (String intrx : outRoute.getIntersections())
        //{
        //    System.out.print(intrx);
        //    if(!intrx.equals(testAgent001.getDestination()))
        //        System.out.print(",");
        //}

        //ArrayList<String> tempPathList  = myMap.getPathByRoad(outRoute.getIntersections());

        //System.out.print("\n\nPath by Link:\n");
        //for (String path : tempPathList)
        //{
        //    System.out.print(path);
        //    if(!tempPathList.isEmpty()
        //       && !path.equals(tempPathList.get(tempPathList.size() - 1)))
        //        System.out.print(",");
        // }

        if(outRoute.getAgent_ID() != "Destination unreachable"){
            logWriter.log(Level.INFO, "Route is: " + outRoute.toString());

            System.out.println("Route is: " + testAgent001.getOrigin() + outRoute.toString()
                    +" "+testAgent001.getDestination());

            System.out.println("\n\nCalculated Travel Time: "+outRoute.getcalculatedTravelTime());
        }
        else{
            System.out.println("Destination Unreachable");
        }

        long stopTime = System.nanoTime();
        long timeToRun = ((stopTime - startTime)/1000000);

        System.out.print("\nTook " + timeToRun/1000.0 + " Seconds");
        System.out.print("\n\nAnd we're done.\n");
    }

    private GRIDagent getTestAgent()
    { // String Id, String newLink, String origin, String destination
        String agtID = "testAgent001",
                currentLink = "401538273_2_r", // 40963664_0 106292026_0 1to2 17005466_7_r
                currentIntrx = "401538273_2_r", // 1040921516 // 1to2
                destIntrx = "17005466_7_r"; // 72823276_0 99to99 24to25 401538273_2_r
        // 864162469 - 1400447055 99282649_0_r [72823276_0 problem link]

        GRIDagent myAgent = new GRIDagent(agtID,currentLink,currentIntrx,destIntrx, false, false);

        return myAgent;
    }
}