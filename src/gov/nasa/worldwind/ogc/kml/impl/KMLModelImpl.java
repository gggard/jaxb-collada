package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Movable3DModel;

public class KMLModelImpl extends Movable3DModel implements KMLRenderable{

	
	public KMLModelImpl(KMLTraversalContext tc, KMLModel kmlmodel)
	{
		super(kmlmodel.getLink().getHref(),
				new Position(
				Angle.fromDegrees(kmlmodel.getLocation().getLatitude()),
        		Angle.fromDegrees(kmlmodel.getLocation().getLongitude()),
        		kmlmodel.getLocation().getAltitude()));
		setUseArdor(true);
		
		//TODO: Implement 3D scaling in models
		double size = kmlmodel.getScale().getX();
		setSize(size);
	}
	
	@Override
	public void preRender(KMLTraversalContext tc, DrawContext dc) {
		// TODO Auto-generated method stub
		// Nothing earth shattering to do in pre-render
	}

	@Override
	public void render(KMLTraversalContext tc, DrawContext dc) {
		// TODO Auto-generated method stub
		super.render(dc);
	}


}
