package edu.ucdenver.cse.GRIDserver;

import org.apache.commons.cli.*;


public class GRIDserverCmdLine {

	private String[] theArgs;
	private Options theOptions;

	public GRIDserverCmdLine(String[] args) {
		this.theArgs = args;
		this.theOptions = new Options();
		
		final Option mapOpt = Option.builder("map")
				                    .argName("map file")
				                    .hasArg(true)
				                    .required(false)
				                    .desc("Map File")
				                    .build();

		this.theOptions.addOption(mapOpt);

		final Option portOpt = Option.builder("port")
				                     .argName("port")
				                     .hasArg(true)
				                     .required(false)
				                     .desc("Server Port")
				                     .build();

		this.theOptions.addOption(portOpt);
		
		final Option outputDirOpt = Option.builder("output")
                                       .argName("Output Directory")
                                       .hasArg(true)
                                       .required(false)
                                       .desc("Output Directory")
                                       .build();
		
		this.theOptions.addOption(outputDirOpt);

		// sim option is a flag, if set we will use sim time, not real time
		final Option simOpt = Option.builder("sim")
                                    .argName("sim flag")
                                    .hasArg(false)
                                    .required(false)
                                    .desc("sim flag")
                                    .build();

		this.theOptions.addOption(simOpt);
		
		final Option weightOpt = Option.builder("weightType")
                                       .argName("weight type")
                                       .hasArg(true)
                                       .required(false)
                                       .desc("weight type")
                                       .build();

		this.theOptions.addOption(weightOpt);
	}
	
	public CommandLine parseArgs() throws ParseException {

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

		return cmd;
	
	}
}
