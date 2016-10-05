package edu.ucdenver.cse.GRIDutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import edu.ucdenver.cse.GRIDmap.*;

public class PopulationGenerator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// Load the map that we are generating FROM
		GRIDmap myMap;
		// Load our version of the map first
		GRIDmapReader masterMap = new GRIDmapReader();
		
		String mapFile = FileUtils.getXmlFile();
		myMap = masterMap.readMapFile(mapFile);
		
		//Drivers distribution on road
		// Define hour here so we can change the range of hours used
		double [] hour = {2.33, 2.33, 2.33, 2.33, 2.33, 2.33, 3.68, 6.00, 7.68, 6.68, 5.33, 4.33, 4.00, 3.68, 4.33, 5.68, 7.33, 5.63, 5.00, 4.68, 4.00, 3.33, 2.33, 2.33};
		DriversDistributionOnRoad distribution = new DriversDistributionOnRoad(hour);
		Random rnd = new Random();
		int range = 1;
		int drivers = 0;

		System.out.print("Enter the number of Agents: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			drivers = Integer.parseInt(br.readLine());
		}
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Using: " + drivers + " drivers");

		double [] drivers_on_road_hourly = distribution.calculateDistribution(drivers, range);
		ArrayList<Integer> times = distribution.generateTimes(drivers_on_road_hourly);
		drivers = times.size();

		//=========================Smart randomizing location=========================
		
		// Get all the links from a file
		
		// this is the OLD way
		//ParseLink pn = new ParseLink();
		
		// randomize these
		RandomizeLocation rndLoc = new RandomizeLocation(myMap);
		
        int randomize_type = 2;//1 or 2
        String work_area = "";
        String home_area = "";
        ArrayList<StartToDestinationLocation> trips = null;
        
        if(randomize_type == 1) //outside to downtown population generation
        {
            work_area = "./data/AlamosaDowntownLinks.txt";
            home_area = "./data/AlamosaLinks.txt";
            trips = rndLoc.generateHomeToWorkLocations(work_area, home_area, drivers, randomize_type);
            
        }
        else if (randomize_type == 2)//full randomization
        {
        	
            work_area = "./data/DenverNetwork.xml"; //home and work area should be the same
            home_area = work_area;
            trips = rndLoc.generateHomeToWorkLocations(work_area, home_area, drivers, randomize_type);
        }
        else
        {
            System.out.println("Please chose:\n1 for outside to downtown population generation,\n2 for full randomization");
            
        }       
        
		//=============================================================================

		//End of drivers distribution on road

		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);

		Network network = sc.getNetwork();
		Population population = sc.getPopulation();
		PopulationFactory populationFactory = population.getFactory();

		int idSeed;

		for(idSeed = 1; idSeed < drivers; ++idSeed){

			Person person = populationFactory.createPerson(Id.createPersonId(idSeed));

			Plan plan = populationFactory.createPlan();
			person.addPlan(plan);

			population.addPerson(person);
			
			
			Id<Link> homeLinkId = Id.createLinkId(trips.get(idSeed).getStartLocation());
			Activity activity1 = populationFactory.createActivityFromLinkId("h", homeLinkId);

			activity1.setEndTime(times.get(idSeed)%86400);

			plan.addActivity(activity1);
			plan.addLeg(populationFactory.createLeg("car"));
			Id<Link> workLinkId = Id.createLinkId(trips.get(idSeed).getDectinationLocation());

			Activity activity2 = populationFactory.createActivityFromLinkId("w", workLinkId);
			activity2.setEndTime((times.get(idSeed)+distribution.generateRandom(0, 28800, rnd))%86400);
			plan.addActivity(activity2);
			plan.addLeg(populationFactory.createLeg("car"));

			Activity activity3 = populationFactory.createActivityFromLinkId("h", homeLinkId);
			activity3.setEndTime((activity2.getEndTime()+distribution.generateRandom(0, 28800, rnd))%86400);
			plan.addActivity(activity3);
			plan.addLeg(populationFactory.createLeg("car"));
		}

		MatsimWriter popWriter = new PopulationWriter(population, network);
				
		String popFileName = FileUtils.chooseFile();
		//String popFileName = "./data/tempPopulation.xml";
		
		if (popFileName != "") {
			popWriter.write(popFileName);
		}

		else {
			System.out.println("\n\nNO OUTPUT FILE SELECTED!!");
		}
	}

}
