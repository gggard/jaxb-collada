/**
 * 
 */
package gov.nasa.worldwind.formats.models.loader;

import gov.nasa.worldwind.formats.models.ModelLoadException;
import gov.nasa.worldwind.formats.models.geometry.Mesh;
import gov.nasa.worldwind.formats.models.geometry.Model;
import junit.framework.TestCase;

/**
 * @author Tisham
 *
 */
public class MaxLoaderTest extends TestCase {

	/**
	 * @param name
	 */
	public MaxLoaderTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link gov.nasa.worldwind.formats.models.loader.MaxLoader#load(java.lang.String)}.
	 */
	public void testLoadString() {
		MaxLoader loader = new MaxLoader();
		Model m = null;
		
		m = loader.load("testmodels/spaceship.3ds");
		 
		assertNotNull(m);
		assertNotSame(0,m.getNumberOfMeshes());
		Mesh mesh = m.getMesh(0);
		assertNotNull(mesh);
		assertNotNull(mesh.vertices);
		assertNotSame(0, mesh.numOfVerts);
		assertNotNull(mesh.faces);
		assertNotSame(0,mesh.numOfFaces);
		assertNotNull(mesh.normals);
		//Max normals equate to number of vertices
		assertEquals(mesh.normals.length, mesh.numOfVerts);
		//Ensure we have a material
		assertNotNull(m.getMaterial(0));
	}

}
