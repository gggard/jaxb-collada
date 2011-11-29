/**
 * Copyright (c) 2008-2010 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package gov.nasa.worldwind.formats.models.collada;

import gov.nasa.worldwind.formats.models.PickableModel;
import gov.nasa.worldwind.render.DrawContext;

import java.io.File;
import java.net.URL;

import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.loader.iLoader;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.jogl.JoglContextCapabilities;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * Simplest example of loading a Collada model.
 * 
 * @author Tisham Dhar
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ArdorColladaLoader implements iLoader
{
	private static final String CONTEXT_KEY = "HACKED CONTEXT";

	public static ColladaNode loadColladaModel(String modelFileStr) throws Exception
	{
		final ColladaNode root = new ColladaNode("rootNode");

		String modelDirStr = new File(modelFileStr).getParent();
		String modelNameStr = new File(modelFileStr).getName();

		File modelDir = new File(modelDirStr);
		modelDirStr = modelDir.getAbsolutePath();

		ColladaImporter importer = new ColladaImporter();
		importer.setLoadAnimations(false);

		SimpleResourceLocator modelLocator = new SimpleResourceLocator(new URL("file:" + modelDirStr));
		SimpleResourceLocator textureLocator = new SimpleResourceLocator(new URL("file:" + modelDirStr));
		importer.setModelLocator(modelLocator);
		importer.setTextureLocator(textureLocator);

		ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, modelLocator);
		ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, textureLocator);

		ColladaStorage storage = importer.load(modelNameStr);
		root.attachChild(storage.getScene());
		root.setAssetData(storage.getAssetData());

		root.updateGeometricState(0);
		return root;
	}

	public static void initializeArdorSystem(final DrawContext dc)
	{
		if (ContextManager.getContextForKey(CONTEXT_KEY) != null)
		{
			ContextManager.switchContext(CONTEXT_KEY);
			return;
		}

		final JoglContextCapabilities caps = new JoglContextCapabilities(dc.getGL());
		final RenderContext rc = new RenderContext(dc.getGLContext(), caps);

		ContextManager.addContext(CONTEXT_KEY, rc);
		ContextManager.switchContext(CONTEXT_KEY);
		
		//disable Ardor3d's frustum culling, as we don't use Ardor3d's camera system:
		Camera cam = new Camera()
		{
			@Override
			public FrustumIntersect contains(BoundingVolume bound)
			{
				return FrustumIntersect.Inside;
			}
		};
		ContextManager.getCurrentContext().setCurrentCamera(cam);
		AWTImageLoader.registerLoader();
	}

	@Override
	public Model load(String path) throws ModelLoadException
	{
		return new PickableModel(path);
	}
}