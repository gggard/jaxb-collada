package gov.nasa.worldwind.formats.models.collada;

import java.util.List;

import com.ardor3d.extension.model.collada.jdom.data.AssetData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Node class that also stores COLLADA {@link AssetData}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColladaNode extends Node
{
	private AssetData assetData;

	public ColladaNode()
	{
		super();
	}

	public ColladaNode(final String name)
	{
		super(name);
	}

	public ColladaNode(final String name, final List<Spatial> children)
	{
		super(name, children);
	}

	public AssetData getAssetData()
	{
		return assetData;
	}

	public void setAssetData(AssetData assetData)
	{
		this.assetData = assetData;
	}
}
