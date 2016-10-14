package edu.ucdenver.cse.GRIDclient;

import org.apache.commons.cli.*;

public class GRIDclientCmdLine {

	private String[] theArgs;
	private final Options theOptions;
	
	public GRIDclientCmdLine(String[] args) {
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
		             
		final Option ipOption = Option.builder("i")
                .argName("ip")
                .hasArg(true)
                .required(false)
                .desc("Server Address")
                .build();
		
		this.theOptions.addOption(ipOption);
		
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
