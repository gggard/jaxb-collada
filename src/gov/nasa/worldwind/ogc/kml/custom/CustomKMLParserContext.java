package gov.nasa.worldwind.ogc.kml.custom;

import gov.nasa.worldwind.ogc.kml.KMLParserContext;
import gov.nasa.worldwind.ogc.kml.custom.CustomKMLPlacemark;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

/**
 * Sets up a custom {@link KMLParserContext} that replaces certain element
 * classes with our custom ones.
 * 
 * @author Tisham Dhar
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLParserContext extends KMLParserContext
{
	public CustomKMLParserContext(XMLEventReader eventReader, String defaultNamespace)
	{
		super(eventReader, defaultNamespace);
	}

	public CustomKMLParserContext(String defaultNamespace)
	{
		super(defaultNamespace);
	}

	public CustomKMLParserContext(CustomKMLParserContext ctx)
	{
		super(ctx);
	}

	@Override
	protected void initializeParsers(String ns)
	{
		super.initializeParsers(ns);
		this.parsers.remove(new QName(ns, "Placemark"));
		this.parsers.put(new QName(ns, "Placemark"), new CustomKMLPlacemark(ns));
		this.parsers.remove(new QName(ns, "Region"));
		this.parsers.put(new QName(ns, "Region"), new CustomKMLRegion(ns));
	}
}
