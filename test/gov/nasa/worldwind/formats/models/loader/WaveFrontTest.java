package gov.nasa.worldwind.formats.models.loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import gov.nasa.worldwind.formats.models.ModelLoadException;
import gov.nasa.worldwind.formats.models.geometry.Material;
import gov.nasa.worldwind.formats.models.geometry.Model;
import junit.framework.TestCase;

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
	}
	
}
