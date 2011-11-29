package gov.nasa.worldwind.ogc.kml.custom;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLParserContext;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMLDoc;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMLDocFactory;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMLInputStream;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMZInputStream;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * <p>
 * {@link KMLRoot} subclass that uses the {@link RelativeKMLDoc} subclasses when
 * constructing {@link KMLDoc}s. It overrides the reference resolving functions
 * to better support relative references.
 * </p>
 * <p>
 * It also overrides the parser context creation method to support our custom
 * parser context {@link CustomKMLParserContext}.
 * </p>
 * 
 * @author Tisham Dhar
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLRoot extends KMLRoot
{
	/**
	 * Creates a KML root for an untyped source. The source must be either a
	 * {@link File}, a {@link URL}, a {@link InputStream}, or a {@link String}
	 * identifying either a file path or a URL. For all types other than
	 * <code>InputStream</code> an attempt is made to determine whether the
	 * source is KML or KMZ; KML is assumed if the test is not definitive. Null
	 * is returned if the source type is not recognized.
	 * 
	 * @param docSource
	 *            either a {@link File}, a {@link URL}, or an
	 *            {@link InputStream}, or a {@link String} identifying a file
	 *            path or URL.
	 * 
	 * @return a new {@link KMLRoot} for the specified source, or null if the
	 *         source type is not supported.
	 * 
	 * @throws IllegalArgumentException
	 *             if the source is null.
	 * @throws IOException
	 *             if an error occurs while reading the source.
	 */
	public static KMLRoot create(Object docSource) throws IOException
	{
		return create(docSource, docSource.toString(), null, true);
	}

	protected static CustomKMLRoot create(Object docSource, String href, KMLDoc parent) throws IOException
	{
		return create(docSource, href, parent, true);
	}

	/**
	 * Creates a KML root for an untyped source. The source must be either a
	 * {@link File}, a {@link URL}, a {@link InputStream}, or a {@link String}
	 * identifying either a file path or a URL. For all types other than
	 * <code>InputStream</code> an attempt is made to determine whether the
	 * source is KML or KMZ; KML is assumed if the test is not definitive. Null
	 * is returned if the source type is not recognized.
	 * 
	 * @param docSource
	 *            either a {@link File}, a {@link URL}, or an
	 *            {@link InputStream}, or a {@link String} identifying a file
	 *            path or URL.
	 * @param namespaceAware
	 *            specifies whether to use a namespace-aware XML parser.
	 *            <code>true</code> if so, <code>false</code> if not.
	 * 
	 * @return a new {@link KMLRoot} for the specified source, or null if the
	 *         source type is not supported.
	 * 
	 * @throws IllegalArgumentException
	 *             if the source is null.
	 * @throws IOException
	 *             if an error occurs while reading the source.
	 */
	public static KMLRoot create(Object docSource, boolean namespaceAware) throws IOException
	{
		return create(docSource, docSource.toString(), null, namespaceAware);
	}

	protected static CustomKMLRoot create(Object docSource, String href, KMLDoc parent, boolean namespaceAware)
			throws IOException
	{
		KMLDoc doc = RelativeKMLDocFactory.createKMLDoc(docSource, null, href, parent);
		if (doc == null)
			return null;
		return new CustomKMLRoot(doc, namespaceAware);
	}

	/**
	 * Creates a KML root for an untyped source and parses it. The source must
	 * be either a {@link File}, a {@link URL}, a {@link InputStream}, or a
	 * {@link String} identifying either a file path or a URL. For all types
	 * other than <code>InputStream</code> an attempt is made to determine
	 * whether the source is KML or KMZ; KML is assumed if the test is not
	 * definitive. Null is returned if the source type is not recognized.
	 * <p/>
	 * Note: Because there are so many incorrectly formed KML files in
	 * distribution, it's often not possible to parse with a namespace aware
	 * parser. This method first tries to use a namespace aware parser, but if a
	 * severe problem occurs during parsing, it will try again using a namespace
	 * unaware parser. Namespace unaware parsing typically bypasses many
	 * problems, but it also causes namespace qualified elements in the XML to
	 * be unrecognized.
	 * 
	 * @param docSource
	 *            either a {@link File}, a {@link URL}, or an
	 *            {@link InputStream}, or a {@link String} identifying a file
	 *            path or URL.
	 * 
	 * @return a new {@link KMLRoot} for the specified source, or null if the
	 *         source type is not supported.
	 * 
	 * @throws IllegalArgumentException
	 *             if the source is null.
	 * @throws IOException
	 *             if an error occurs while reading the source.
	 * @throws javax.xml.stream.XMLStreamException
	 *             if the KML file has severe errors.
	 */
	public static KMLRoot createAndParse(Object docSource) throws IOException, XMLStreamException
	{
		return createAndParse(docSource, docSource.toString(), null);
	}

	protected static CustomKMLRoot createAndParse(Object docSource, String linkRef, KMLDoc parent) throws IOException,
			XMLStreamException
	{
		CustomKMLRoot kmlRoot = CustomKMLRoot.create(docSource, linkRef, parent);

		if (kmlRoot == null)
		{
			String message =
					Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource", docSource.toString());
			throw new IllegalArgumentException(message);
		}

		try
		{
			// Try with a namespace aware parser.
			kmlRoot.parse();
		}
		catch (XMLStreamException e)
		{
			// Try without namespace awareness.
			kmlRoot = CustomKMLRoot.create(docSource, linkRef, parent, false);
			kmlRoot.parse();
		}

		return kmlRoot;
	}

	/**
	 * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc
	 * represents KML and KMZ files from either files or input streams.
	 * 
	 * @param docSource
	 *            the KMLDoc instance representing the KML document.
	 * 
	 * @throws IllegalArgumentException
	 *             if the document source is null.
	 * @throws IOException
	 *             if an error occurs while reading the KML document.
	 */
	protected CustomKMLRoot(KMLDoc docSource) throws IOException
	{
		this(docSource, true);
	}

	/**
	 * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc
	 * represents KML and KMZ files from either files or input streams.
	 * 
	 * @param docSource
	 *            the KMLDoc instance representing the KML document.
	 * @param namespaceAware
	 *            specifies whether to use a namespace-aware XML parser.
	 *            <code>true</code> if so, <code>false</code> if not.
	 * 
	 * @throws IllegalArgumentException
	 *             if the document source is null.
	 * @throws IOException
	 *             if an error occurs while reading the KML document.
	 */
	protected CustomKMLRoot(KMLDoc docSource, boolean namespaceAware) throws IOException
	{
		super(docSource, namespaceAware);
	}


	/**
	 * Create a new <code>KMLRoot</code> with a specific namespace. (The default
	 * namespace is defined by
	 * {@link gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
	 * 
	 * @param namespaceURI
	 *            the default namespace URI.
	 * @param docSource
	 *            the KML source specified via a {@link KMLDoc} instance. A
	 *            KMLDoc represents KML and KMZ files from either files or input
	 *            streams.
	 * 
	 * @throws IllegalArgumentException
	 *             if the document source is null.
	 * @throws java.io.IOException
	 *             if an I/O error occurs attempting to open the document
	 *             source.
	 */
	protected CustomKMLRoot(String namespaceURI, KMLDoc docSource) throws IOException
	{
		this(namespaceURI, docSource, true);
	}

	/**
	 * Create a new <code>KMLRoot</code> with a specific namespace. (The default
	 * namespace is defined by
	 * {@link gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
	 * 
	 * @param namespaceURI
	 *            the default namespace URI.
	 * @param docSource
	 *            the KML source specified via a {@link KMLDoc} instance. A
	 *            KMLDoc represents KML and KMZ files from either files or input
	 *            streams.
	 * @param namespaceAware
	 *            specifies whether to use a namespace-aware XML parser.
	 *            <code>true</code> if so, <code>false</code> if not.
	 * 
	 * @throws IllegalArgumentException
	 *             if the document source is null.
	 * @throws java.io.IOException
	 *             if an I/O error occurs attempting to open the document
	 *             source.
	 */
	protected CustomKMLRoot(String namespaceURI, KMLDoc docSource, boolean namespaceAware) throws IOException
	{
		super(namespaceURI, docSource, namespaceAware);
	}

	@Override
	protected KMLParserContext createParserContext(XMLEventReader reader)
	{
		/*String[] mimeTypes = new String[] {KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE};
		XMLEventParserContextFactory.addParserContext(mimeTypes, new CustomKMLParserContext(this.getNamespaceURI()));
		return super.createParserContext(reader);*/

		return this.parserContext = new CustomKMLParserContext(reader, this.getNamespaceURI());
	}


	/* ***********************************************************************************
	 * The following functions are overridden to pass the original 'link' through to the *
	 * resolveLocalReference function.                                                   *
	 *********************************************************************************** */

	@Override
	public Object resolveNetworkLink(String link, boolean cacheRemoteFile, long updateTime)
	{
		if (link == null)
		{
			String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Object o = null;
		try
		{
			// Interpret the path relative to the current document.
			String path = this.getSupportFilePath(link);
			if (path == null)
				path = link;

			// If the file is eligible for caching, check the session cache to see if it has already been retrieved and
			// parsed.
			if (cacheRemoteFile)
			{
				o = WorldWind.getSessionCache().get(path);
				if (o instanceof KMLRoot)
					return o;
			}

			URL url = WWIO.makeURL(path);
			if (url == null)
			{
				// See if the reference can be resolved to a local file.
				o = this.resolveLocalReference(path, null, link);
			}

			// If we didn't find a local file, treat it as a remote reference.
			if (o == null)
			{
				url = WorldWind.getDataFileStore().requestFile(path, cacheRemoteFile);
				if (url != null)
				{
					// Check the file's modification time against the link update time. If the file was last modified
					// earlier than the link update time then we need to remove the cached file from the file store,
					// and start a new file retrieval.
					File file = new File(url.toURI());
					if (file.lastModified() < updateTime)
					{
						WorldWind.getDataFileStore().removeFile(link);
					}
				}

				// Call resolveRemoteReference to retrieve and parse the file.
				o = this.resolveRemoteReference(path, null, cacheRemoteFile);
			}
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.UnableToResolveReference", link);
			Logging.logger().warning(message);
		}

		return o;
	}

	@Override
	public Object resolveReference(String link, boolean cacheRemoteFile)
	{
		if (link == null)
		{
			String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			String[] linkParts = link.split("#");
			String linkBase = linkParts[0];
			String linkRef = linkParts.length > 1 ? linkParts[1] : null;

			// See if it's a reference to an internal element.
			if (WWUtil.isEmpty(linkBase) && !WWUtil.isEmpty(linkRef))
				return this.getItemByID(linkRef);

			// Interpret the path relative to the current document.
			String path = this.getSupportFilePath(linkBase);
			if (path == null)
				path = linkBase;

			// See if it's an already found and parsed KML file.
			Object o = WorldWind.getSessionCache().get(path);
			if (o != null && o instanceof KMLRoot)
				return linkRef != null ? ((KMLRoot) o).getItemByID(linkRef) : o;

			URL url = WWIO.makeURL(path);
			if (url == null)
			{
				// See if the reference can be resolved to a local file.
				o = this.resolveLocalReference(path, linkRef, linkBase);
			}

			// If we didn't find a local file, treat it as a remote reference.
			if (o == null)
				o = this.resolveRemoteReference(path, linkRef, cacheRemoteFile);

			if (o != null)
				return o;

			// If the reference was not resolved as a remote reference, look for a local element identified by the
			// reference string. This handles the case of malformed internal references that omit the # sign at the
			// beginning of the reference.
			return this.getItemByID(link);
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.UnableToResolveReference", link);
			Logging.logger().warning(message);
		}

		return null;
	}


	/* ***************************************************************************************
	 * The following functions are overridden to create new CustomKMLRoot and RelativeKMLDoc *
	 * objects that support the relative 'href'.                                             *
	 *************************************************************************************** */

	@Override
	public Object resolveLocalReference(String linkBase, String linkRef)
	{
		return resolveLocalReference(linkBase, linkRef, null);
	}

	public Object resolveLocalReference(String linkBase, String linkRef, String href)
	{
		if (linkBase == null)
		{
			String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			File file = new File(linkBase);

			// Determine whether the file is a KML or KMZ. If it's not just return the original address.
			if (!file.exists() || !WWIO.isContentType(file, KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE))
				return linkBase;

			// Attempt to open and parse the KML/Z file, trying both namespace aware and namespace unaware stream
			// readers if necessary.
			KMLRoot refRoot = CustomKMLRoot.createAndParse(file, href, getKMLDoc());
			// An exception is thrown if parsing fails, so no need to check for null.

			// Add the parsed file to the session cache so it doesn't have to be parsed again.
			WorldWind.getSessionCache().put(linkBase, refRoot);

			// Now check the newly opened KML/Z file for the referenced item, if a reference was specified.
			if (linkRef != null)
				return refRoot.getItemByID(linkRef);
			else
				return refRoot;
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.UnableToResolveReference", linkBase + "/" + linkRef);
			Logging.logger().warning(message);
			return null;
		}
	}

	@Override
	protected KMLRoot parseCachedKMLFile(URL url, String linkBase, String contentType, boolean namespaceAware)
			throws IOException, XMLStreamException
	{
		KMLDoc kmlDoc;

		InputStream refStream = url.openStream();

		if (KMLConstants.KMZ_MIME_TYPE.equals(contentType))
			kmlDoc = new RelativeKMZInputStream(refStream, WWIO.makeURI(linkBase), linkBase, getKMLDoc());
		else
			// Attempt to parse as KML
			kmlDoc = new RelativeKMLInputStream(refStream, WWIO.makeURI(linkBase), linkBase, getKMLDoc());

		try
		{
			KMLRoot refRoot = new CustomKMLRoot(kmlDoc, namespaceAware);
			refRoot.parse(); // also closes the URL's stream
			return refRoot;
		}
		catch (XMLStreamException e)
		{
			refStream.close(); // parsing failed, so explicitly close the stream
			throw e;
		}
	}
}
