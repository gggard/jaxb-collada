package org.csiro.examples.model;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.ApplicationTemplate;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.event.KeyEvent;

public class FullScreenTest extends ApplicationTemplate {

	public static class AppFrame extends ApplicationTemplate.AppFrame {
		public AppFrame() {
			super(true, false, false);
			this.getWwd().setModel(new BasicModel());
			GraphicsConfiguration config = this.getGraphicsConfiguration();
			GraphicsDevice device = config.getDevice();

			if(device.isFullScreenSupported())
			{
				try {
					this.setUndecorated(true);
			        this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
					this.setResizable(false);
					this.getWwd().setPreferredSize(new Dimension(1000,800));
					device.setFullScreenWindow(this);
					
				} finally {
					device.setFullScreenWindow(null);
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Frame frame = new Frame("WorldwindFull");
		final WorldWindowGLCanvas worldWindowGLCanvas = new WorldWindowGLCanvas();
		worldWindowGLCanvas.setModel(new BasicModel());

		worldWindowGLCanvas.addKeyListener(new java.awt.event.KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		});

		frame.add(worldWindowGLCanvas);
		frame.setSize(640, 480);
		frame.setUndecorated(true);
		int size = frame.getExtendedState();
		size |= Frame.MAXIMIZED_BOTH;
		frame.setExtendedState(size);

		frame.setVisible(true);
		worldWindowGLCanvas.requestFocus();
	}
}
