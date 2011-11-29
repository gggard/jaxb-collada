package gov.nasa.worldwind.ogc.kml.relativeio;

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.ogc.kml.io.KMLInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * The {@link RelativeKMLInputStream} class is a subclass of
 * {@link KMLInputStream} that supports better resolving of relative KML
 * references.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RelativeKMLInputStream extends KMLInputStream implements RelativeKMLDoc
{
	private final String href;
	private final KMLDoc parent;

	public RelativeKMLInputStream(InputStream sourceStream, URI uri, String href, KMLDoc parent) throws IOException
	{
		super(sourceStream, uri);
		this.href = href;
		this.parent = parent;
	}

	@Override
	public String getHref()
	{
		return href;
	}

	@Override
	public KMLDoc getParent()
	{
		return parent;
	}

	@Override
	public boolean isContainer()
	{
		return false;
	}

	@Override
	public InputStream getSupportFileStream(String path) throws IOException
	{
		path = RelativizedPath.normalizePath(path);
		RelativizedPath relativized = RelativizedPath.relativizePath(path, this);
		path = relativized.path;
		if (relativized.relativeTo != this)
		{
			return relativized.relativeTo.getSupportFileStream(path);
		}

		return super.getSupportFileStream(path);
	}

	@Override
	public String getSupportFilePath(String path)
	{
		path = RelativizedPath.normalizePath(path);
		RelativizedPath relativized = RelativizedPath.relativizePath(path, this);
		path = relativized.path;
		if (relativized.relativeTo != this)
		{
			try
			{
				return relativized.relativeTo.getSupportFilePath(path);
			}
			catch (IOException e)
			{
				//ignore
			}
		}

		return super.getSupportFilePath(path);
	}
}
