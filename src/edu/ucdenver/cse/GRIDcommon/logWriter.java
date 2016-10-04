package edu.ucdenver.cse.GRIDcommon;


import java.io.IOException;
import java.util.logging.*;

public class logWriter {
	static Logger myErrLogger;
	public Handler errFileHandler;
	public Formatter errFormatter;
	
	static Logger myInfoLogger;
	public Handler infoFileHandler;
	public Formatter infoFormatter;
	
	private logWriter() throws IOException {
		
		 
		
		myErrLogger = Logger.getLogger(logWriter.class.getName());
		errFormatter = new SimpleFormatter();
		errFileHandler = new FileHandler("TEST_ERR_LOG.log",false);
		errFileHandler.setLevel(Level.WARNING);
		errFileHandler.setFormatter(errFormatter);
		
		myInfoLogger = Logger.getLogger(logWriter.class.getName());
		infoFormatter = new SimpleFormatter();
		infoFileHandler = new FileHandler("TEST_INFO_LOG.log", false);
		infoFileHandler.setLevel(Level.INFO);
		infoFileHandler.setFormatter(infoFormatter);
		
		myErrLogger.addHandler(errFileHandler);
		myInfoLogger.addHandler(infoFileHandler);
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
}