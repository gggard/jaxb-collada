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
public class ColladaTest extends TestCase {

	/**
	 * @param name
	 */
	public ColladaTest(String name) {
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
	 * Test method for {@link gov.nasa.worldwind.formats.models.loader.ColladaLoader#load(java.lang.String)}.
	 */
	public void testLoad() {
		ColladaLoader loader = new ColladaLoader();
		Model m = null;
		try {
			m = loader.load("testmodels/collada.dae");
		} catch (ModelLoadException e) {
			//e.printStackTrace();
		}
		assertNotNull(m);
		assertNotSame(0,m.getNumberOfMeshes());
		Mesh mesh = m.getMesh(0);
		assertNotNull(mesh);
		assertNotNull(mesh.vertices);
		assertNotSame(0, mesh.numOfVerts);
		assertNotNull(mesh.faces);
		assertNotSame(0,mesh.numOfFaces);
		assertNotNull(mesh.normals);
		//Collada normals equate to number of faces
		assertEquals(mesh.normals.length, mesh.numOfFaces);
	}

}
