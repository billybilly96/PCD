package mapmonitor.common;

import java.util.Random;

import assignment.utility.Point2d;
import assignment.utility.Vector2d;

public class UtilitySensor {

	public static int getCurrentPatch(Point2d position) {
		double currentPosX = position.getX();
		double currentPosY = position.getY();
		if (currentPosX < UtilityValues.MAP_MIN_X || currentPosX > UtilityValues.MAP_MAX_X || currentPosY < UtilityValues.MAP_MIN_Y || currentPosY > UtilityValues.MAP_MAX_Y) {
			return -1;
		} else {
			double patchSizeX = (UtilityValues.MAP_MAX_X - UtilityValues.MAP_MIN_X) / UtilityValues.M;
			double patchSizeY = (UtilityValues.MAP_MAX_Y - UtilityValues.MAP_MIN_Y) / UtilityValues.N;
			int px = (int) Math.ceil(currentPosX / patchSizeX);
			int py = (int) Math.ceil(currentPosY / patchSizeY);
			int m = (int) ((UtilityValues.MAP_MAX_X - UtilityValues.MAP_MIN_X) / patchSizeX);
			return px + (m * (py - 1));
		}
	}

	public static Point2d initializePosition() {
		double newPosX = getRandomDouble(UtilityValues.MAP_MIN_X, UtilityValues.MAP_MAX_X);
		double newPosY = getRandomDouble(UtilityValues.MAP_MIN_Y, UtilityValues.MAP_MAX_Y);
		return new Point2d(newPosX, newPosY);
	}

	public static Vector2d initializeDeltaPosition() {
		double newDeltaX = getRandomDouble((UtilityValues.MAP_MIN_X - UtilityValues.MAP_MAX_X) / 20, (UtilityValues.MAP_MAX_X - UtilityValues.MAP_MIN_X) / 20);
		double newDeltaY = getRandomDouble((UtilityValues.MAP_MIN_Y - UtilityValues.MAP_MAX_Y) / 20, (UtilityValues.MAP_MAX_Y - UtilityValues.MAP_MIN_Y) / 20);
		return new Vector2d(newDeltaX, newDeltaY);
	}

	public static double initializeValue() {
		return getRandomDouble(UtilityValues.MIN_VALUE, UtilityValues.MAX_VALUE);
	}

	public static double initializeDeltaValue() {
		return getRandomDouble((UtilityValues.MIN_VALUE - UtilityValues.MAX_VALUE) / 20, (UtilityValues.MAX_VALUE - UtilityValues.MIN_VALUE) / 20);
	}

	private static double getRandomDouble(double min, double max) {
		Random rand = new Random();
		return min + (max - min) * rand.nextDouble();
	}
	
}