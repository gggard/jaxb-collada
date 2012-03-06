package gov.nasa.worldwind.formats.models.loader;

import junit.framework.TestCase;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.loader.WaveFrontLoader;

public class WaveFrontTest extends TestCase {

	public WaveFrontTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLoad() {
		WaveFrontLoader loader = new WaveFrontLoader();
		
		Model model = null;
		try {
			model = loader.load("testmodels/penguin.obj");
		} catch (ModelLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertNotNull(model);
		assertNotSame(0,model.getNumberOfMeshes());
		Mesh mesh = model.getMesh(0);
		assertNotNull(mesh);
		assertNotNull(mesh.vertices);
		assertNotSame(0, mesh.numOfVerts);
		assertNotNull(mesh.faces);
		assertNotSame(0,mesh.numOfFaces);
		assertNotNull(mesh.normals);
		//Max normals equate to number of faces
		//assertEquals(mesh.normals.length, mesh.numOfFaces);
		//Ensure we have a material
		assertNotNull(model.getMaterial(0));
		//We have a texture materials string file is not null
		assertNotNull(model.getMaterial(0).strFile);
		assertTrue(model.getMaterial(0).strFile.
				equalsIgnoreCase("penguin.gif"));
	}
	
}
