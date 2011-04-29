package org.csiro.examples.model;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.firstperson.FlyToFlyViewAnimator;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FullScreenTest{

	/**
	 * The animated view
	 */
	public static BasicFlyView view;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		view = new BasicFlyView();
		Frame frame = new Frame("WorldwindFull");
		final WorldWindowGLCanvas worldWindowGLCanvas = new WorldWindowGLCanvas();
		worldWindowGLCanvas.setModel(new BasicModel());
		worldWindowGLCanvas.setView(view);

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
		
		setUpTimer();
	}

	public static void moveToLocation(Position pos) {
		if (pos == null) {
			return;
		}
		double elevation = view.getGlobe().getElevation(pos.getLatitude(),
				pos.getLongitude());
		FlyToFlyViewAnimator animator = FlyToFlyViewAnimator.
				createFlyToFlyViewAnimator(view, view.getEyePosition(),
						new Position(pos.latitude, pos.longitude, elevation),
						view.getHeading(), view.getHeading(), view.getPitch(),
						view.getPitch(), view.getEyePosition().getElevation(),
						view.getEyePosition().getElevation(), 10000, 0);
		view.addAnimator(animator);
		animator.start();
		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	public static void setUpTimer()
	{
		int delay = 5000;   // delay for 5 sec.
		int period = 10000;  // repeat every sec.
		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	Position pos = new Position(Angle.fromDegrees(
		        			180*new Random().nextFloat()-90.0),
		        								Angle.fromDegrees(
		        			360*new Random().nextFloat()-180.0),
		        								0.0);
		            moveToLocation(pos);
		        }
		    }, delay, period);
	}
}
