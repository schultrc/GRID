package edu.ucdenver.cse.GRIDclient;

import org.apache.commons.cli.*;

public class GRIDclientCmdLine {

	private String[] theArgs;
	private final Options theOptions;
	
	public GRIDclientCmdLine(String[] args) {
		this.theArgs = args;
		this.theOptions = new Options();
		
		final Option mapOption = Option.builder("map")
				                 .argName("mapfile")
				                 .hasArg(true)
				                 .required(false)
				                 .desc("Map File")
				                 .build();
		
		this.theOptions.addOption(mapOption);
		
		final Option portOption = Option.builder("port")
				                  .argName("port")
				                  .hasArg(true)
				                  .required(false)
				                  .desc("Server Port")
				                  .build();
		
		this.theOptions.addOption(portOption);
		             
		final Option ipOption = Option.builder("ip")
                .argName("ip")
                .hasArg(true)
                .required(false)
                .desc("Server Address")
                .build();
		
		this.theOptions.addOption(ipOption);
	
		final Option agentTries = Option.builder("a")
                .argName("agentTries")
                .hasArg(true)
                .required(false)
                .desc("Number of agents to send")
                .build();
		
		this.theOptions.addOption(agentTries);
	
		final Option outputDir = Option.builder("output")
                .argName("Output Directory")
                .hasArg(true)
                .required(false)
                .desc("Output Directory")
                .build();
		
		this.theOptions.addOption(outputDir);
	}
	
	public CommandLine parseArgs() throws ParseException{

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			   cmd = parser.parse(theOptions, theArgs);	
		}
		catch (ParseException e) {
			// This is bad, what should we do?
			System.out.println("Parser error - invalid input");
			throw e;
		}
		
		for(Option opt:cmd.getOptions()) {
			System.out.println("Option: " + opt.toString());
		}

		System.out.println(cmd.getOptionObject("m"));

		
		return cmd;
	}
}
