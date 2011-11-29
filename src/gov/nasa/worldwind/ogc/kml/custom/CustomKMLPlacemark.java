package gov.nasa.worldwind.ogc.kml.custom;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLLineString;
import gov.nasa.worldwind.ogc.kml.KMLLinearRing;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLMultiGeometry;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLPoint;
import gov.nasa.worldwind.ogc.kml.KMLPolygon;
import gov.nasa.worldwind.ogc.kml.custom.impl.KMLColladaModel;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A {@link KMLPlacemark} subclass that creates a {@link KMLModelImpl} object
 * for KML &lt;Model&gt; elements.
 * </p>
 * <p>
 * It also overrides the placement geometry initialization function to create
 * geometry on a thread separate from the render thread. This increases
 * perceived performance.
 * </p>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLPlacemark extends KMLPlacemark
{
	protected Object renderablesLock = new Object();
	protected final Set<KMLAbstractGeometry> geomsRequestedLoad = new HashSet<KMLAbstractGeometry>();

	public CustomKMLPlacemark(String namespaceURI)
	{
		super(namespaceURI);
	}

	@Override
	protected void addRenderable(KMLRenderable r)
	{
		synchronized (renderablesLock)
		{
			super.addRenderable(r);
		}
	}

	@Override
	protected void doPreRender(KMLTraversalContext tc, DrawContext dc)
	{
		synchronized (renderablesLock)
		{
			super.doPreRender(tc, dc);
		}
	}

	@Override
	protected void doRender(KMLTraversalContext tc, DrawContext dc)
	{
		synchronized (renderablesLock)
		{
			super.doRender(tc, dc);
		}
	}

	@Override
	protected KMLRenderable selectModelRenderable(KMLTraversalContext tc, KMLAbstractGeometry geom)
	{
		return new KMLColladaModel(tc, (KMLModel) geom);
	}

	@Override
	protected void initializeGeometry(KMLTraversalContext tc, KMLAbstractGeometry geom)
	{
		if (geom == null)
			return;

		if (this.getRenderables() == null)
			this.renderables = new ArrayList<KMLRenderable>(1); // most common case is one renderable

		if (!geomsRequestedLoad.contains(geom))
		{
			if (!WorldWind.getTaskService().isFull())
			{
				WorldWind.getTaskService().addTask(new InitializeGeometryTask(tc, geom));
				geomsRequestedLoad.add(geom);
			}
		}
	}

	protected class InitializeGeometryTask implements Runnable
	{
		protected final KMLTraversalContext tc;
		protected final KMLAbstractGeometry geom;

		public InitializeGeometryTask(KMLTraversalContext tc, KMLAbstractGeometry geom)
		{
			this.tc = tc;
			this.geom = geom;
		}

		@Override
		public void run()
		{
			if (geom instanceof KMLPoint)
				addRenderable(selectPointRenderable(tc, geom));
			else if (geom instanceof KMLLinearRing) // since LinearRing is a subclass of LineString, this test must precede
				addRenderable(selectLinearRingRenderable(tc, geom));
			else if (geom instanceof KMLLineString)
				addRenderable(selectLineStringRenderable(tc, geom));
			else if (geom instanceof KMLPolygon)
				addRenderable(selectPolygonRenderable(tc, geom));
			else if (geom instanceof KMLMultiGeometry)
			{
				List<KMLAbstractGeometry> geoms = ((KMLMultiGeometry) geom).getGeometries();
				if (geoms != null)
				{
					for (KMLAbstractGeometry g : geoms)
					{
						initializeGeometry(tc, g); // recurse
					}
				}
			}
			else if (geom instanceof KMLModel)
				addRenderable(selectModelRenderable(tc, geom));
		}
	}
}
