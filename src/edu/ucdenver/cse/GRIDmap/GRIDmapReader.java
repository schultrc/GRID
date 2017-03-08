package edu.ucdenver.cse.GRIDmap;

import javax.xml.parsers.*;
import org.xml.sax.*;

import edu.ucdenver.cse.GRIDutil.FileUtils;

import javax.xml.parsers.SAXParserFactory;

public class GRIDmapReader {

	public GRIDmap readMapFile(String mapFile){
			
		if (mapFile == "") {
			mapFile = FileUtils.getXmlFile(); 
		}
		
		try {	
	         SAXParserFactory factory = SAXParserFactory.newInstance();
	         SAXParser saxParser = factory.newSAXParser();
	         
	         XMLReader xmlreader = saxParser.getXMLReader();
	         	         
	         GRIDmapParser GMP = new GRIDmapParser();
	         xmlreader.setContentHandler(GMP);
	      
	         System.out.println("Trying to open: " + mapFile);
	         
	         xmlreader.parse(new InputSource(mapFile));	             
	         
	         GRIDmap theMap = GMP.getMap();
	         
	         // For now, init the map once done - this makes it ready to go
	         	         
	         return theMap;
	    } 
		catch (Exception e) {
	         e.printStackTrace();
	         
		 return null;
	    }
	}
}
