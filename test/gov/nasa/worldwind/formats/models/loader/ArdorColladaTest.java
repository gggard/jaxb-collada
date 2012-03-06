package gov.nasa.worldwind.formats.models.loader;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import gov.nasa.worldwind.formats.models.collada.ArdorColladaLoader;

import net.java.joglutils.model.ResourceRetriever;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.scenegraph.Node;

public class ArdorColladaTest {

	@Before
	public void setUp() throws Exception {
		AWTImageLoader.registerLoader();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore("Fails to execute in Ant")
	public void testLoadColladaModel() {
		Node colladaNode = null;
		
		try {
			colladaNode = ArdorColladaLoader.loadColladaModel("testmodels/superdome.dae");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(colladaNode);
		try {
			colladaNode = ArdorColladaLoader.loadColladaModel("testmodels/models/model.dae");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(colladaNode);
		try {
			colladaNode = ArdorColladaLoader.loadColladaModel("testmodels/models/model_png.dae");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(colladaNode);
	}

}
