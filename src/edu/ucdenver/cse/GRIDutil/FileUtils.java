package edu.ucdenver.cse.GRIDutil;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileUtils {
	
	public static String chooseFile() {
		JFileChooser chooser = new JFileChooser();
	    
	    chooser.setCurrentDirectory(new File("./"));
	    
	    return showChooser(chooser);
	}
	
	// Specific to return a MATSIM config file
	public static String getXmlFile() {

		JFileChooser chooser = new JFileChooser();
	    chooser.setCurrentDirectory(new File("./"));
	    
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
	            "xml config files", "xml");
	    chooser.setFileFilter(filter);
	    
	    return showChooser(chooser);
	}
	
	public static String showChooser(JFileChooser chooser) {
		
	    int returnVal = chooser.showOpenDialog(null);
        
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	            
	        return chooser.getSelectedFile().toString();
	    }
	    
	    else {
	    	return "";
	    }
	}
}
