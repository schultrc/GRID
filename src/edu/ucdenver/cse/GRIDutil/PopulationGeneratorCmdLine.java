package edu.ucdenver.cse.GRIDutil;

import org.apache.commons.cli.*;

public class PopulationGeneratorCmdLine {
    private String[] theArgs;
    private final Options theOptions;

    public PopulationGeneratorCmdLine(String[] args){
        this.theArgs = args;
        this.theOptions = new Options();

        // enter the name for the population file
        final Option networkFileName = Option.builder("mapfile")
                                    .argName("Map File Name")
                                    .hasArg(true)
                                    .required(false)
                                    .desc("Map File Name")
                                    .build();

        this.theOptions.addOption(networkFileName);

        // enter the output directory where you want to save the file
        final Option popFileName = Option.builder("popfile")
                                    .argName("Population File Name")
                                    .hasArg(true)
                                    .required(false)
                                    .desc("Population File Name")
                                    .build();

        this.theOptions.addOption(popFileName);
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
