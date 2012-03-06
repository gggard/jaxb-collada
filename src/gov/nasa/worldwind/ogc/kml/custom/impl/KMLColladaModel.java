package gov.nasa.worldwind.ogc.kml.custom.impl;

import net.java.joglutils.model.ModelLoadException;
import gov.nasa.worldwind.formats.models.collada.ColladaModel;
import gov.nasa.worldwind.formats.models.collada.ColladaNode;
import gov.nasa.worldwind.formats.models.collada.KMLArdorColladaLoader;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLRoot;

public class KMLColladaModel extends ColladaModel
{
	protected final KMLRoot kmlroot;
	protected final String href;
	
	public KMLColladaModel(String path, Position position, KMLRoot kmlroot, String href) throws ModelLoadException
	{
		super(path, position);
		this.kmlroot = kmlroot;
		this.href = href;
	}

	@Override
	protected ColladaNode loadModel(String source) throws Exception
	{
		return KMLArdorColladaLoader.loadColladaModel(source, href, kmlroot);
	}
}
