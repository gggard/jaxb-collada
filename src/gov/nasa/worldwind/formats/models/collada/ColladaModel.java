package gov.nasa.worldwind.formats.models.collada;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.formats.models.Adjustable;
import gov.nasa.worldwind.formats.models.PickableModelFactory;
import gov.nasa.worldwind.formats.models.PickableModel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;

import java.util.concurrent.atomic.AtomicReference;

import javax.media.opengl.GL;

import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector4;
import com.ardor3d.math.type.ReadOnlyMatrix4;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.record.TextureStateRecord;
import com.ardor3d.renderer.state.record.TextureUnitRecord;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.geom.Debugger;

/**
 * Object that represents a COLLADA model, using the Ardor3D library.
 * 
 * @author Tisham Dhar
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColladaModel implements Renderable, Scene, Adjustable
{
	static
	{
		//TODO what does this actually do?
		TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
	}

	/**
	 * Simple subclass of the {@link JoglCanvasRenderer} that creates a
	 * JoglRenderer object immediately on construction.
	 */
	protected class SimpleJoglCanvasRenderer extends JoglCanvasRenderer
	{
		private final JoglRenderer joglRenderer = new JoglRenderer();

		public SimpleJoglCanvasRenderer(Scene scene)
		{
			super(scene);
		}

		@Override
		public Renderer getRenderer()
		{
			return joglRenderer;
		}
	};

	private static final Log LOG = LogFactory.getLog(ColladaModel.class);

	protected final String path;
	protected Model model;

	protected Position position;
	protected int altitudeMode = WorldWind.CLAMP_TO_GROUND;

	protected Angle yaw = Angle.ZERO;
	protected Angle roll = Angle.ZERO;
	protected Angle pitch = Angle.ZERO;
	protected Vec4 scale = new Vec4(1, 1, 1);
	protected double unitScale = 1;

	protected boolean constantSize = true;
	protected Vec4 referenceCenterPoint;
	protected double size = 1;

	protected final AtomicReference<ColladaNode> nodeRef = new AtomicReference<ColladaNode>();
	protected JoglCanvasRenderer renderer = new SimpleJoglCanvasRenderer(this);

	protected boolean visible = true;
	protected boolean requestedLoad = false;

	protected Matrix4 cachedRotationMatrix;
	protected boolean cachedRotationMatrixDirty = true;

	public ColladaModel(String path, Position position) throws ModelLoadException
	{
		this.path = path;
		this.model = PickableModelFactory.createModel(path);
		this.setPosition(position);
		this.model.setUseLighting(true);
		this.model.setUseTexture(true);
	}

	@Override
	public void render(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (!this.isVisible())
			return;

		try
		{
			beginDraw(dc);
			if (model instanceof PickableModel)
			{
				((PickableModel) this.model).setRenderPicker(dc.isPickingMode());
			}
			draw(dc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			endDraw(dc);
		}
	}

	/**
	 * Draw the model (after checking model intersects Camera's frustum)
	 * 
	 * @param dc
	 *            Draw context
	 */
	protected void draw(DrawContext dc)
	{
		GL gl = dc.getGL();
		this.referenceCenterPoint = this.computeReferenceCenter(dc);
		Vec4 loc = referenceCenterPoint;
		double localSize = this.computeSize(dc, loc) * unitScale;
		ReadOnlyMatrix4 rotationMatrix = getRotationMatrix();
		Frustum frustum = dc.getView().getFrustumInModelCoordinates();

		//Check that the model is within the camera's frustum; this involves converting from
		//Ardor3D's BoundingSphere to WorldWind's Sphere, as the Math libraries are different.
		boolean intersects = true;
		Node node = nodeRef.get();
		BoundingVolume volume = node == null ? null : node.getWorldBound();

		//Currently only the BoundingSphere is supported, but Ardor3D collada models seem to
		//always use spheres as their bounding volume.
		//TODO Add support for other BoundingVolumes
		if (volume instanceof BoundingSphere)
		{
			//We could use the Ardor3D Transform object here to transform the BoundingSphere
			//into world coordinates. However, doing it manually allows us to reuse the 4x4
			//rotation matrix later.

			//first rotate, scale, and translate the sphere's center
			ReadOnlyVector3 center3 = volume.getCenter();
			Vector4 center = new Vector4(center3.getX(), center3.getY(), center3.getZ(), 1);
			center = rotationMatrix.applyPost(center, null);
			center.set(center.getX() * localSize * scale.x, center.getY() * localSize * scale.y, center.getZ()
					* localSize * scale.z, 1);
			center.addLocal(loc.x, loc.y, loc.z, 0);

			//next scale the sphere's radius
			double radius = ((BoundingSphere) volume).getRadius();
			double maxScale =
					Math.max(Math.abs(scale.getX()), Math.max(Math.abs(scale.getY()), Math.abs(scale.getZ())));
			radius *= Math.abs(localSize * maxScale);

			//finally check that the bounding sphere is within the frustum
			Sphere sphere = new Sphere(new Vec4(center.getX(), center.getY(), center.getZ()), radius);
			intersects = frustum.intersects(sphere);
		}
		else
		{
			//The model hasn't been loaded yet, or has an unsupported bounding volume.
			//Simply check that the model's location is within the Camera's frustum.
			intersects = frustum.contains(loc);
		}

		if (intersects)
		{
			try
			{
				//translate to model center
				dc.getView().pushReferenceCenter(dc, loc);

				//load the rotation matrix
				double[] rotationMatrixD = new double[16];
				rotationMatrix.toArray(rotationMatrixD, false);
				gl.glMultMatrixd(rotationMatrixD, 0);

				//scale the model
				gl.glScaled(localSize * scale.x, localSize * scale.y, localSize * scale.z);

				//draw!
				drawArdor(dc);
				//drawBounds();
			}
			finally
			{
				dc.getView().popReferenceCenter(dc);
			}
		}
	}

	/**
	 * @return 4x4 rotation matrix for this model.
	 */
	protected ReadOnlyMatrix4 getRotationMatrix()
	{
		//Same as:
		//gl.glRotated(position.getLongitude().degrees, 0, 1, 0);
		//gl.glRotated(-position.getLatitude().degrees, 1, 0, 0);
		//gl.glRotated(-yaw.degrees, 0, 0, 1);
		//gl.glRotated(-pitch.degrees, 1, 0, 0);
		//gl.glRotated(-roll.degrees, 0, 1, 0);

		if (cachedRotationMatrixDirty)
		{
			ReadOnlyMatrix4 r1;
			{
				double y1 = position.getLongitude().radians;
				double x2 = -position.getLatitude().radians;
				double c1 = Math.cos(y1);
				double s1 = Math.sin(y1);
				double c2 = Math.cos(x2);
				double s2 = Math.sin(x2);
				//Left-handed YX rotation (see http://en.wikipedia.org/wiki/Euler_angles)
				r1 = new Matrix4(c1, s2 * s1, c2 * s1, 0, 0, c2, -s2, 0, -s1, s2 * c1, c2 * c1, 0, 0, 0, 0, 1);
			}

			ReadOnlyMatrix4 r2;
			{
				double z1 = -yaw.radians;
				double x2 = -pitch.radians;
				double y3 = -roll.radians;
				double c1 = Math.cos(z1);
				double s1 = Math.sin(z1);
				double c2 = Math.cos(x2);
				double s2 = Math.sin(x2);
				double c3 = Math.cos(y3);
				double s3 = Math.sin(y3);
				//Left-handed ZXY rotation (see http://en.wikipedia.org/wiki/Euler_angles)
				r2 =
						new Matrix4(c1 * c3 - s1 * s2 * s3, -c2 * s1, c1 * s3 + c3 * s1 * s2, 0,
								c3 * s1 + c1 * s2 * s3, c1 * c2, s1 * s3 - c1 * c3 * s2, 0, -c2 * s3, s2, c2 * c3, 0,
								0, 0, 0, 1);
			}

			cachedRotationMatrix = r1.multiply(r2, cachedRotationMatrix);
			cachedRotationMatrixDirty = false;
		}
		return cachedRotationMatrix;
	}

	/**
	 * Use Ardor3D to draw this model. This function also loads the model (on a
	 * separate thread) if it hasn't been loaded yet.
	 * 
	 * @param dc
	 *            Draw context
	 */
	protected void drawArdor(DrawContext dc)
	{
		ArdorColladaLoader.initializeArdorSystem(dc);

		Node node = this.nodeRef.get();

		if (node == null && !requestedLoad)
		{
			if (!WorldWind.getTaskService().isFull())
			{
				WorldWind.getTaskService().addTask(new LoadModelTask());
				requestedLoad = true;
			}
		}

		if (node != null)
		{
			GL gl = dc.getGL();

			if (model.isUsingTexture())
			{
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glEnable(GL.GL_BLEND);
				gl.glEnable(GL.GL_RESCALE_NORMAL);
			}
			else
			{
				gl.glDisable(GL.GL_TEXTURE_2D);
				gl.glDisable(GL.GL_BLEND);
			}

			final RenderContext context = ContextManager.getCurrentContext();
			final ContextCapabilities caps = context.getCapabilities();
			final TextureStateRecord record = (TextureStateRecord) context.getStateRecord(StateType.Texture);
			for (int i = 0; i < caps.getNumberOfTotalTextureUnits(); i++)
			{
				TextureUnitRecord unitRecord = record.units[i];
				unitRecord.invalidate();
			}

			node.draw(renderer.getRenderer());
			renderer.getRenderer().renderBuckets();
		}
	}

	/**
	 * Draw the bounds of the model using the Ardor3D {@link Debugger} class.
	 */
	protected void drawBounds()
	{
		Node node = this.nodeRef.get();
		if (node != null)
		{
			Debugger.setBoundsColor((ColorRGBA) ColorRGBA.WHITE);
			Debugger.drawBounds(node.getWorldBound(), renderer.getRenderer());
			renderer.getRenderer().renderBuckets();
		}
	}

	/**
	 * Load a Collada model from a source path. This method should be overridden
	 * by subclasses who use custom model loaders.
	 * 
	 * @param source
	 *            Source path of the model.
	 * @return {@link ColladaNode} containing the COLLADA model.
	 * @throws Exception
	 */
	protected ColladaNode loadModel(String source) throws Exception
	{
		return ArdorColladaLoader.loadColladaModel(model.getSource());
	}

	/**
	 * Load this object's model.
	 */
	protected void loadModel()
	{
		try
		{
			ColladaNode node = loadModel(model.getSource());
			node.updateWorldBound(true);
			unitScale = node.getAssetData().getUnitMeter();
			nodeRef.set(node);
			ensureMaterialsHaveLights(node);
		}
		catch (Exception e)
		{
			LOG.error("Failed to load model", e);
		}
	}

	/**
	 * Ardor3D disables OpenGL lighting if no lighting state has been set on the
	 * Mesh. OpenGL ignores materials if there is no lighting. This function
	 * checks for Meshes that contain a material state, and if they do, sets a
	 * light state on them to ensure the material is lit.
	 * 
	 * @param spatial
	 *            Root node of the render tree to check.
	 */
	protected void ensureMaterialsHaveLights(Spatial spatial)
	{
		if (spatial instanceof Node)
		{
			Node node = (Node) spatial;
			for (Spatial child : node.getChildren())
			{
				ensureMaterialsHaveLights(child);
			}
		}
		else if (spatial instanceof Mesh)
		{
			Mesh mesh = (Mesh) spatial;

			//OpenGL doesn't use the glMaterial render state if lighting is disabled. Therefore, if
			//this mesh has a Material state, we need to add a lighting state if it doesn't already
			//have one so that lighting is not disabled. But don't enable lighting if the model is
			//textured. Not sure if this is the correct solution, but it works with the models tested.
			if (mesh.getWorldRenderState(StateType.Material) != null
					&& mesh.getWorldRenderState(StateType.Light) == null
					&& mesh.getWorldRenderState(StateType.Texture) == null)
			{
				LightState lightState = new LightState();
				lightState.setGlobalAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
				lightState.setEnabled(true);
				spatial.setRenderState(lightState);
				spatial.updateWorldRenderStates(true);
			}
		}
	}

	protected class LoadModelTask implements Runnable
	{
		@Override
		public void run()
		{
			loadModel();
		}
	}

	/**
	 * Set up the OpenGL state for rendering this model. Also pushes the state
	 * and matrix information onto their relative stacks for later restoration.
	 * 
	 * @param dc
	 *            Draw context
	 */
	protected void beginDraw(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glPushAttrib(GL.GL_TEXTURE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_HINT_BIT
				| GL.GL_POLYGON_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_TRANSFORM_BIT
				| GL.GL_CLIENT_VERTEX_ARRAY_BIT);
		gl.glPushClientAttrib((int) GL.GL_ALL_CLIENT_ATTRIB_BITS);

		Vec4 cameraPosition = dc.getView().getEyePoint();
		float[] lightPosition = { (float) cameraPosition.x, (float) cameraPosition.y, (float) cameraPosition.z, 1.0f };
		float[] lightAmbient = { 0.4f, 0.4f, 0.4f, 0.4f };
		float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] model_ambient = { 0.5f, 0.5f, 0.5f, 1.0f };
		gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);
		gl.glDisable(GL.GL_LIGHT0);
		gl.glEnable(GL.GL_LIGHT1);
		gl.glEnable(GL.GL_LIGHTING);

		gl.glEnable(GL.GL_NORMALIZE);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
	}

	/**
	 * Pops the OpenGL state pushed in the
	 * {@link ColladaModel#beginDraw(DrawContext)} function.
	 * 
	 * @param dc
	 *            Draw context
	 */
	protected void endDraw(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glPopAttrib();
		gl.glPopClientAttrib();
	}

	/**
	 * For models that don't have a fixed size, compute the model size for the
	 * current eye point.
	 * 
	 * @param dc
	 *            Draw context
	 * @param loc
	 *            Model's location
	 * @return Size scale to use
	 */
	protected double computeSize(DrawContext dc, Vec4 loc)
	{
		if (this.constantSize)
		{
			return size;
		}
		if (loc == null)
		{
			LOG.error("Null location when computing size of model");
			return 1;
		}
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double newSize = 60 * dc.getView().computePixelSizeAtDistance(d);
		if (newSize < 2)
		{
			newSize = 2;
		}
		return newSize;
	}

	/**
	 * Calculate the model's location in world coordinates.
	 * 
	 * @param dc
	 *            Draw context
	 * @return Model's location
	 */
	protected Vec4 computeReferenceCenter(DrawContext dc)
	{
		double elevation = 0;
		if (altitudeMode != WorldWind.ABSOLUTE)
		{
			elevation += dc.getGlobe().getElevation(position.getLatitude(), position.getLongitude());
		}
		if (altitudeMode != WorldWind.CLAMP_TO_GROUND)
		{
			elevation += position.elevation;
		}
		return dc.getGlobe().computePointFromPosition(position, elevation * dc.getVerticalExaggeration());
	}

	public boolean isConstantSize()
	{
		return constantSize;
	}

	public void setConstantSize(boolean constantSize)
	{
		this.constantSize = constantSize;
	}

	public double getSize()
	{
		return size;
	}

	public void setSize(double size)
	{
		this.size = size;
	}

	public Position getPosition()
	{
		return position;
	}

	public void setPosition(Position position)
	{
		this.position = position;
		cachedRotationMatrixDirty = true;
	}

	public Model getModel()
	{
		return model;
	}

	@Override
	public Angle getYaw()
	{
		return yaw;
	}

	@Override
	public void setYaw(Angle yaw)
	{
		this.yaw = yaw;
		cachedRotationMatrixDirty = true;
	}

	@Override
	public Angle getRoll()
	{
		return roll;
	}

	@Override
	public void setRoll(Angle roll)
	{
		this.roll = roll;
		cachedRotationMatrixDirty = true;
	}

	@Override
	public Angle getPitch()
	{
		return pitch;
	}

	@Override
	public void setPitch(Angle pitch)
	{
		this.pitch = pitch;
		cachedRotationMatrixDirty = true;
	}

	public Vec4 getScale()
	{
		return scale;
	}

	public void setScale(Vec4 scale)
	{
		this.scale = scale;
	}

	public int getAltitudeMode()
	{
		return altitudeMode;
	}

	public void setAltitudeMode(int altitudeMode)
	{
		this.altitudeMode = altitudeMode;
	}

	public boolean isVisible()
	{
		return this.visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public boolean renderUnto(Renderer renderer)
	{
		return false;
	}

	@Override
	public PickResults doPick(Ray3 pickRay)
	{
		return null;
	}
}
