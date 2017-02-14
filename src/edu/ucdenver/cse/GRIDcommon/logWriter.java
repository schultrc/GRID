package edu.ucdenver.cse.GRIDcommon;


import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class logWriter {
	static Logger myErrLogger;
	private static Handler errFileHandler;
	private static Formatter errFormatter;
	
	static Logger myInfoLogger;
	private static Handler infoFileHandler;
	private static Formatter infoFormatter;
	
	private static Path outputDir;
	private static boolean loggerInit;
	
	private static String filePrefix;
	
	private logWriter() throws IOException {
	
		String errFilePath;
		String infoFilePath;
		boolean appendFlag = false;
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		
		// If we have set the outputDir, do not append, overwrite
		if (loggerInit) {
			errFilePath = outputDir  + "/" + timeStamp + "_ERR_" + filePrefix + ".log";
			infoFilePath = outputDir + "/" + timeStamp + "_INFO_" + filePrefix + ".log";
			appendFlag = false;
		}

		// Otherwise, append so we always have the output
		else {
			errFilePath = "./"  + timeStamp + "_ERR_" + filePrefix + ".log";
			infoFilePath = "./" + timeStamp + "_INFO_" + filePrefix + ".log";

			appendFlag = true;
		}

		if (myErrLogger == null) {
			myErrLogger = Logger.getLogger(logWriter.class.getName());
			errFormatter = new SimpleFormatter();						
			errFileHandler = new FileHandler(errFilePath, appendFlag);				
			errFileHandler.setLevel(Level.WARNING);
			errFileHandler.setFormatter(errFormatter);
			
			//myErrLogger.setUseParentHandlers(false);
			myErrLogger.addHandler(errFileHandler);
		}

		if (myInfoLogger == null) {
			myInfoLogger = Logger.getLogger(logWriter.class.getName());
			infoFormatter = new SimpleFormatter();
			infoFileHandler = new FileHandler(infoFilePath, appendFlag);
			infoFileHandler.setLevel(Level.INFO);
			infoFileHandler.setFormatter(infoFormatter);
			
			// Do not output to the console
			myInfoLogger.setUseParentHandlers(false);
			myInfoLogger.addHandler(infoFileHandler);
		}		
	}
	
	private static Logger getErrLogger() {
		if (myErrLogger == null) {
			try {
				new logWriter();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return myErrLogger;
	}
	
	private static Logger getInfoLogger() {
		if (myInfoLogger == null) {
			try {
				new logWriter();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return myInfoLogger;
	}
    
	public static void log(Level level, String msg) {
		if(level.equals(Level.WARNING)) {
			getErrLogger().log(level, msg);

		}
		
		else if (level.equals(Level.INFO)) {
			getInfoLogger().log(level, msg);
		}
	}
	
	public static void setOutputDir(Path theDir) {
		outputDir = theDir;
		loggerInit = true;
	}
	
	public static void setLogPrefix(String prefix) {
		filePrefix = prefix;
	}

	public static void stop() {
		try {
			infoFileHandler.close();
			errFileHandler.close();
		}
		
		finally {
			System.out.println("We are done");
		}
		
	}
}