package gov.nasa.worldwind.formats.models.collada;

import gov.nasa.worldwind.ogc.kml.KMLRoot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.util.resource.ResourceLocator;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

/**
 * Specialized COLLADA model loader that uses a {@link KMLRoot} object to
 * resolve relative references (such as textures within KMZ files).
 * 
 * @author Tisham Dhar
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class KMLArdorColladaLoader
{
	public static ColladaNode loadColladaModel(String modelFileStr, String modelHref, final KMLRoot kmlRoot)
			throws Exception
	{
		final ColladaNode root = new ColladaNode("rootNode");

		File modelFile = new File(modelFileStr);
		String modelNameStr = modelFile.getName();
		File modelDir = modelFile.getParentFile();

		int indexOfLastSlash = Math.max(modelHref.lastIndexOf('/'), modelHref.lastIndexOf('\\'));
		final String modelHrefDir = (indexOfLastSlash >= 0 ? modelHref.substring(0, indexOfLastSlash) + "/" : "");

		ColladaImporter importer = new ColladaImporter();
		importer.setLoadAnimations(false); //turns off the verbose "no animation" logging

		SimpleResourceLocator modelLocator = new SimpleResourceLocator(modelDir.toURI());
		importer.setModelLocator(modelLocator);
		importer.setTextureLocator(new ResourceLocator()
		{
			@Override
			public ResourceSource locateResource(String resourceName)
			{
				try
				{
					return new URLResourceSource(new URL("file:"
							+ (String) kmlRoot.resolveReference(modelHrefDir + resourceName)));
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
					return null;
				}
			}
		});

		ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, modelLocator);

		ColladaStorage storage = importer.load(modelNameStr);
		root.attachChild(storage.getScene());
		root.setAssetData(storage.getAssetData());

		root.updateGeometricState(0);
		return root;
	}
}
