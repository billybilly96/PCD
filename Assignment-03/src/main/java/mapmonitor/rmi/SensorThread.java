package mapmonitor.rmi;

import javax.swing.SwingUtilities;

import assignment.utility.Pair;
import assignment.utility.Point2d;
import mapmonitor.common.UtilityValues;
import mapmonitor.common.ViewSensor;

public class SensorThread extends Thread {

	private Sensor mySensor;
	private ViewSensor viewSensor;

	public SensorThread(Sensor sensor) {
		mySensor = sensor;
		viewSensor = new ViewSensor();
	}

	@Override
	public void run() {
		long prevMillis = System.currentTimeMillis();
		SwingUtilities.invokeLater(() -> viewSensor.setVisible(true));
		while (true) {
			try {
				if (System.currentTimeMillis() - prevMillis < UtilityValues.SENSOR_TIMER_MILLIS) {
					Thread.sleep(UtilityValues.SENSOR_TIMER_MILLIS - (System.currentTimeMillis() - prevMillis));
				}
				prevMillis = System.currentTimeMillis();
				double newValue = mySensor.updateValue();
				Pair<Integer, Point2d> newPosition = mySensor.updatePosition();
				viewSensor.update(newPosition.getX(), newPosition.getY(), newValue);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void interrupt() {
		SwingUtilities.invokeLater(() -> viewSensor.setVisible(false));
		super.interrupt();
	}

}