package mapmonitor.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import assignment.utility.Pair;
import assignment.utility.Point2d;

public interface SensorRemote extends Remote {

	public Pair<Integer, Point2d> getPosition() throws RemoteException;

	public double getValue(int patch) throws RemoteException, OutFromPatchException;

}