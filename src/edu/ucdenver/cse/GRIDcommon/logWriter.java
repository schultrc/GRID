package edu.ucdenver.cse.GRIDcommon;


import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.*;

public class logWriter {
	static Logger myErrLogger;
	private Handler errFileHandler;
	private Formatter errFormatter;
	
	static Logger myInfoLogger;
	private Handler infoFileHandler;
	private Formatter infoFormatter;
	
	private static Path outputDir;
	private static boolean loggerInit;
	
	private logWriter() throws IOException {
	
		if (myErrLogger == null) {
			myErrLogger = Logger.getLogger(logWriter.class.getName());
			errFormatter = new SimpleFormatter();
			
			String filePath; 
			
			// If we have set the outputDir, do not append, overwrite
			if (loggerInit) {
				filePath = outputDir + "/TEST_ERR_LOG.log";
				errFileHandler = new FileHandler(filePath, false);

			}
			
			// Otherwise, append so we always have the output
			else {
				filePath = "./output/TEST_ERR_LOG.log";
				errFileHandler = new FileHandler(filePath, false);
			}
							
			errFileHandler.setLevel(Level.WARNING);
			errFileHandler.setFormatter(errFormatter);
			
			
			myErrLogger.addHandler(errFileHandler);
		}

		if (myInfoLogger == null) {
			myInfoLogger = Logger.getLogger(logWriter.class.getName());
			infoFormatter = new SimpleFormatter();
			infoFileHandler = new FileHandler(outputDir + "TEST_INFO_LOG.log", false);
			infoFileHandler.setLevel(Level.INFO);
			infoFileHandler.setFormatter(infoFormatter);
			
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
	
}