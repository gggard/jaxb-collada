package org.csiro.parser.collada;

import java.util.List;

import javax.xml.stream.events.XMLEvent;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

public class ColladaFloatArrayParser extends AbstractXMLEventParser {
	
	@Override
	/**
	 * Create lists from geometry float arrays
	 */
	public List parse(XMLEventParserContext ctx, XMLEvent doubleEvent, Object... args)
	{
		return null;
	}
}
