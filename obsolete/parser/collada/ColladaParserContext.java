package org.csiro.parser.collada;

import gov.nasa.worldwind.ogc.kml.KMLCoordinatesParser;
import gov.nasa.worldwind.util.xml.BasicXMLEventParserContext;

public class ColladaParserContext extends BasicXMLEventParserContext {
	
	/** Names of fields with float array */
	protected ColladaFloatArrayParser floatArrayParser;
	
	/** The names of elements that contain merely string data and can be parsed by a generic string parser. */
    protected static final String[] StringFields = new String[]
	    {
	        // Only element names, not attribute names, are needed here.
	        
	    };
    /** The names of elements that contain merely double data and can be parsed by a generic double parser. */
    protected static final String[] DoubleFields = new String[]
        {
    		
        };
    
    
    /** The names of elements that contain merely integer data and can be parsed by a generic integer parser. */
    protected static final String[] IntegerFields = new String[]
	   {
    	
	   };
    
    /**
     * The names of elements that contain merely boolean integer (0 or 1) data and can be parsed by a generic boolean
     * integer parser.
     */
    protected static final String[] BooleanIntegerFields = new String[]
        {
    	
        };                                                           
}
