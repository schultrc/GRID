package edu.ucdenver.cse.GRIDsim;

import org.apache.commons.cli.*;


public class GRIDsimCmdLine {
	
	private String[] theArgs;
	private final Options theOptions;
	
	public GRIDsimCmdLine(String[] args) {
		this.theArgs = args;
		this.theOptions = new Options();
	
		final Option mapOption = Option.builder("config")
				                       .argName("config file")
				                       .hasArg(true).required(false)
				                       .desc("MATSIM config file")
				                       .build();

		this.theOptions.addOption(mapOption);

		final Option portOption = Option.builder("port")
				                        .argName("port")
				                        .hasArg(true)
				                        .required(false)
				                        .desc("Server Port").build();

		this.theOptions.addOption(portOption);

		final Option ipOption = Option.builder("ip")
				                      .argName("ip")
				                      .hasArg(true)
				                      .required(false)
				                      .desc("Server Address")
				                      .build();

		this.theOptions.addOption(ipOption);	
	
		final Option outputDir = Option.builder("output")
                					   .argName("Output Directory")
                					   .hasArg(true)
                					   .required(false)
                					   .desc("Output Directory")
                					   .build();
		
		this.theOptions.addOption(outputDir);
		
		final Option agentCtrl = Option.builder("AgtCtrl")
									   .argName("Agent Control Percent")
									   .hasArg(true)
									   .required(false)
									   .desc("Agent Control Percent")
									   .build();
		
		this.theOptions.addOption(agentCtrl);
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

		return cmd;
	}
	
	public String toString() {
		// Return the string of all command args
		
		String theArgString = "";
		
		return theArgString;
	}
}
