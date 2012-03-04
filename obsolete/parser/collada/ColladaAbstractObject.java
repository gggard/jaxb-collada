package org.csiro.parser.collada;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;

public abstract class ColladaAbstractObject extends AbstractXMLEventParser{
	/**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected ColladaAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Returns the version of this object, if any.
     *
     * @return the version of this object, or null if it's not specified in the element.
     */
    public String getVersion()
    {
        return (String) this.getField("version");
    }

    public ColladaRoot getRoot()
    {
        XMLEventParser root = super.getRoot();
        return root instanceof ColladaRoot ? (ColladaRoot) root : null;
    }
}
