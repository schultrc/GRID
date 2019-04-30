package edu.ucdenver.cse.GRIDutil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class GRIDPopGenCmdLine {
	private String[] theArgs;
	private final Options theOptions;
	
	public GRIDPopGenCmdLine(String[] args) {
		this.theArgs = args;
		this.theOptions = new Options();
	
		final Option mapOption = Option.builder("mapFile")
				                       .argName("map file")
				                       .hasArg(true).required(false)
				                       .desc("MATSIM Map file")
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
                					   .argName("Output File Name")
                					   .hasArg(true)
                					   .required(false)
                					   .desc("Output File Name")
                					   .build();
		
		this.theOptions.addOption(outputDir);
		
		final Option agentCountOpt = Option.builder("agentCount")
									   .argName("Agent Count")
									   .hasArg(true)
									   .required(false)
									   .desc("Agent Count")
									   .build();
		
		this.theOptions.addOption(agentCountOpt);
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
