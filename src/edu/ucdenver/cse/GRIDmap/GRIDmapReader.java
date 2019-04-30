package edu.ucdenver.cse.GRIDmap;

import javax.xml.parsers.*;
import org.xml.sax.*;

import edu.ucdenver.cse.GRIDutil.FileUtils;

public class GRIDmapReader {

	public GRIDmap readMapFile(String mapFile){
			
		if (mapFile == "") {
			mapFile = FileUtils.getXmlFile(); 
		}
		
		try {	
	         SAXParserFactory factory = SAXParserFactory.newInstance();
	         SAXParser saxParser = factory.newSAXParser();
	         
	         XMLReader xmlreader = saxParser.getXMLReader();
	         
	         // Change this to use a different map input type
	         GRIDmapParser GMP = new GRIDmapParser();
	         xmlreader.setContentHandler(GMP);
	      
	         System.out.println("Trying to open: " + mapFile);
	         
	         xmlreader.parse(new InputSource(mapFile));	             
	         
	         GRIDmap theMap = GMP.getMap();
	                  	         
	         return theMap;
	    } 
		catch (Exception e) {
	         e.printStackTrace();
	         
		 return null;
	    }
	}
}
