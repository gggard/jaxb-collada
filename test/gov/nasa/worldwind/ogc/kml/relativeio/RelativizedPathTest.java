package gov.nasa.worldwind.ogc.kml.relativeio;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class RelativizedPathTest
{
	private final String hrefKmz1 = "dir1/dir2/sydney.kmz";
	private final String hrefKmz2 = "dir3/dir4/melbourne.kmz";
	private final String hrefKml = "model/model.kml";
	private final String testZipFile = "ZipFile.zip";
	private final String testModelFile = "Model.kml";

	@Test
	public void testRelativizePath1() throws IOException
	{
		InputStream kmzis = getClass().getResourceAsStream(testZipFile);
		InputStream kmlis = getClass().getResourceAsStream(testModelFile);
		RelativeKMLDoc kmzFile = new RelativeKMZInputStream(kmzis, null, hrefKmz1, null);
		RelativeKMLDoc modelFile = new RelativeKMLInputStream(kmlis, null, hrefKml, kmzFile);
		RelativizedPath relativized = RelativizedPath.relativizePath("icon/pin.png", modelFile);

		assertEquals("model/icon/pin.png", relativized.path);
		assertEquals(kmzFile, relativized.relativeTo);
	}

	@Test
	public void testRelativizePath2() throws IOException
	{
		InputStream kmzis = getClass().getResourceAsStream(testZipFile);
		InputStream kmlis = getClass().getResourceAsStream(testModelFile);
		RelativeKMLDoc kmzFile = new RelativeKMZInputStream(kmzis, null, hrefKmz1, null);
		RelativeKMLDoc modelFile = new RelativeKMLInputStream(kmlis, null, hrefKml, kmzFile);
		RelativizedPath relativized = RelativizedPath.relativizePath("../icon/pin.png", modelFile);

		assertEquals("icon/pin.png", relativized.path); //icon/pin.png in the zip file
		assertEquals(kmzFile, relativized.relativeTo);
	}

	@Test
	public void testRelativizePath3() throws IOException
	{
		InputStream kmzis = getClass().getResourceAsStream(testZipFile);
		InputStream kmlis = getClass().getResourceAsStream(testModelFile);
		RelativeKMLDoc kmzFile = new RelativeKMZInputStream(kmzis, null, hrefKmz1, null);
		RelativeKMLDoc modelFile = new RelativeKMLInputStream(kmlis, null, hrefKml, kmzFile);
		RelativizedPath relativized = RelativizedPath.relativizePath("../../icon/pin.png", modelFile);

		assertEquals("icon/pin.png", relativized.path); //icon/pin.png in the directory of the zip file
		assertEquals(kmzFile, relativized.relativeTo);
	}

	@Test
	public void testRelativizePath4() throws IOException
	{
		InputStream kmz1is = getClass().getResourceAsStream(testZipFile);
		InputStream kmz2is = getClass().getResourceAsStream(testZipFile);
		InputStream kmlis = getClass().getResourceAsStream(testModelFile);
		RelativeKMLDoc kmz1File = new RelativeKMZInputStream(kmz1is, null, hrefKmz1, null);
		RelativeKMLDoc kmz2File = new RelativeKMZInputStream(kmz2is, null, hrefKmz2, kmz1File);
		RelativeKMLDoc modelFile = new RelativeKMLInputStream(kmlis, null, hrefKml, kmz2File);
		RelativizedPath relativized = RelativizedPath.relativizePath("../icon/pin.png", modelFile);

		assertEquals("icon/pin.png", relativized.path);
		assertEquals(kmz2File, relativized.relativeTo);
	}
	
	@Test
	public void testRelativizePath5() throws IOException
	{
		InputStream kmz1is = getClass().getResourceAsStream(testZipFile);
		InputStream kmz2is = getClass().getResourceAsStream(testZipFile);
		InputStream kmlis = getClass().getResourceAsStream(testModelFile);
		RelativeKMLDoc kmz1File = new RelativeKMZInputStream(kmz1is, null, hrefKmz1, null);
		RelativeKMLDoc kmz2File = new RelativeKMZInputStream(kmz2is, null, hrefKmz2, kmz1File);
		RelativeKMLDoc modelFile = new RelativeKMLInputStream(kmlis, null, hrefKml, kmz2File);
		RelativizedPath relativized = RelativizedPath.relativizePath("../../../../icon/pin.png", modelFile);
		
		assertEquals("icon/pin.png", relativized.path);
		assertEquals(kmz1File, relativized.relativeTo);
	}
	
	@Test
	public void testRelativizePath6() throws IOException
	{
		InputStream kmz1is = getClass().getResourceAsStream(testZipFile);
		InputStream kmz2is = getClass().getResourceAsStream(testZipFile);
		InputStream kmlis = getClass().getResourceAsStream(testModelFile);
		RelativeKMLDoc kmz1File = new RelativeKMZInputStream(kmz1is, null, hrefKmz1, null);
		RelativeKMLDoc kmz2File = new RelativeKMZInputStream(kmz2is, null, hrefKmz2, kmz1File);
		RelativeKMLDoc modelFile = new RelativeKMLInputStream(kmlis, null, hrefKml, kmz2File);
		RelativizedPath relativized = RelativizedPath.relativizePath("../../../../../../icon/pin.png", modelFile);
		
		assertEquals("../icon/pin.png", relativized.path);
		assertEquals(kmz1File, relativized.relativeTo);
	}

	@Test
	public void testNormalizePath1()
	{
		String unnormalized = "file://///dir1/dir2//dir3///dir4\\dir5\\\\dir6\\\\\\dir7/../dir8/../../dir9";
		String expected = "file://///dir1/dir2/dir3/dir4/dir5/dir9";
		String actual = RelativizedPath.normalizePath(unnormalized);
		assertEquals(expected, actual);
	}

	@Test
	public void testNormalizePath2()
	{
		String unnormalized = "../../dir1/../dir2/dir3";
		String expected = "../../dir2/dir3";
		String actual = RelativizedPath.normalizePath(unnormalized);
		assertEquals(expected, actual);
	}
}
