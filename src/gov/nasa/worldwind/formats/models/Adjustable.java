package gov.nasa.worldwind.formats.models;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

/**
 * @author ringo-wathelet 2008-06-11
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Adjustable
{
	void setYaw(Angle val);
	Angle getYaw();
	void setRoll(Angle val);
	Angle getRoll();
	void setPitch(Angle val);
	Angle getPitch();
	void setSize(double val);
	double getSize();
	void setPosition(Position val);
	Position getPosition();
}
