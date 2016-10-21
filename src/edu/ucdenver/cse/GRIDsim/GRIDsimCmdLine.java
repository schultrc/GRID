package edu.ucdenver.cse.GRIDsim;

import org.apache.commons.cli.*;


public class GRIDsimCmdLine {
	
	private String[] theArgs;
	private final Options theOptions;
	
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
