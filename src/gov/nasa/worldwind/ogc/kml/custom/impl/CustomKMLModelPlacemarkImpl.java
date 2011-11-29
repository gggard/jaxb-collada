package gov.nasa.worldwind.ogc.kml.custom.impl;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLLocation;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLOrientation;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLScale;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import net.java.joglutils.model.ModelLoadException;

public class CustomKMLModelPlacemarkImpl implements KMLRenderable
{
	protected final KMLModel model;
	protected final KMLPlacemark parent;
	protected final KMLColladaModel ardorModel;

	/**
	 * Create an instance.
	 * 
	 * @param tc
	 *            the current {@link KMLTraversalContext}.
	 * @param placemark
	 *            the <i>Placemark</i> element containing the <i>Point</i>.
	 * @param geom
	 *            the {@link gov.nasa.worldwind.ogc.kml.KMLPoint} geometry.
	 * 
	 * @throws NullPointerException
	 *             if the geometry is null.
	 * @throws IllegalArgumentException
	 *             if the parent placemark or the traversal context is null.
	 */
	public CustomKMLModelPlacemarkImpl(KMLTraversalContext tc, KMLPlacemark placemark, KMLAbstractGeometry geom)
	{
		if (tc == null)
		{
			String msg = Logging.getMessage("nullValue.TraversalContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (placemark == null)
		{
			String msg = Logging.getMessage("nullValue.ParentIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (geom == null)
		{
			String msg = Logging.getMessage("nullValue.GeometryIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		this.model = (KMLModel) geom;
		this.parent = placemark;
		String href = model.getLink().getHref();

		Object reference = model.getRoot().resolveReference(href);
		if (reference == null || !(reference instanceof String))
		{
			String msg = "Unable to resolve model reference: " + href;
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		String path = (String) reference;

		Position position = locationToPosition(model.getLocation());
		if (position == null)
		{
			String msg = "Unable to calculate model position: " + href;
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		try
		{
			ardorModel = new KMLColladaModel(path, position, model.getRoot(), href);

			String altitudeMode = model.getAltitudeMode();
			if (altitudeMode != null)
			{
				ardorModel.setAltitudeMode(KMLUtil.convertAltitudeMode(altitudeMode));
			}

			KMLOrientation orientation = model.getOrientation();
			if (orientation != null)
			{
				Double tilt = orientation.getTilt(), roll = orientation.getRoll(), heading = orientation.getHeading();
				if (tilt != null)
					ardorModel.setPitch(Angle.fromDegrees(tilt));
				if (roll != null)
					ardorModel.setRoll(Angle.fromDegrees(roll));
				if (heading != null)
					ardorModel.setYaw(Angle.fromDegrees(heading));
			}

			KMLScale scale = model.getScale();
			if (scale != null)
			{
				Double sx = scale.getX(), sy = scale.getY(), sz = scale.getZ();
				ardorModel.setScale(new Vec4(sx != null ? sx : 1, sy != null ? sy : 1, sz != null ? sz : 1));
			}
		}
		catch (ModelLoadException e)
		{
			//this should never happen, as the iLoader implementation (ArdorColladaLoader) doesn't
			//load the model immediately, but loads it lazily in the model's loadModel function

			String msg = "Error loading model: " + href;
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	protected static Position locationToPosition(KMLLocation location)
	{
		if (location == null)
			return null;

		Double lat = location.getLatitude(), lon = location.getLongitude(), alt = location.getAltitude();
		return new Position(Angle.fromDegrees(lat != null ? lat : 0), Angle.fromDegrees(lon != null ? lon : 0),
				alt != null ? alt : 0);
	}

	@Override
	public void preRender(KMLTraversalContext tc, DrawContext dc)
	{
	}

	@Override
	public void render(KMLTraversalContext tc, DrawContext dc)
	{
		ardorModel.render(dc);
	}
}
