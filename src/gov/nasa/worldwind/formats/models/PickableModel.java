package gov.nasa.worldwind.formats.models;

import net.java.joglutils.model.geometry.Model;

/**
 * {@link Model} subclass that adds picking support.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PickableModel extends Model
{
	protected boolean renderPicker = false;

	public PickableModel(String source)
	{
		super(source);
	}

	public boolean isRenderPicker()
	{
		return renderPicker;
	}

	public void setRenderPicker(boolean renderPicker)
	{
		this.renderPicker = renderPicker;
	}
}
