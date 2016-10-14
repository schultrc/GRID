package edu.ucdenver.cse.GRIDserver;

import org.apache.commons.cli.*;


public class GRIDserverCmdLine {

	private String[] theArgs;
	private Options theOptions;

	public GRIDserverCmdLine(String[] args) {
		this.theArgs = args;
		this.theOptions = new Options();
		final Option mapOption = Option.builder("m")
				                       .argName("mapfile")
				                       .hasArg(true)
				                       .required(false)
				                       .desc("Map File")
				                       .build();

		this.theOptions.addOption(mapOption);

		final Option portOption = Option.builder("p")
				                        .argName("port")
				                        .hasArg(true)
				                        .required(false)
				                        .desc("Server Port")
				                        .build();

		this.theOptions.addOption(portOption);
		
		this.theOptions.addOption("a", "agentPercent", false, "agent control percentage");
	}
	
	public CommandLine parseArgs() {

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			   cmd = parser.parse(theOptions, theArgs);	
		}
		catch (ParseException e) {
			// This is bad, what should we do?
			System.out.println("Parser error - invalid input");
		}

		return cmd;
	
	}
}
