package mapmonitor.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import mapmonitor.common.StateGuardian;

public interface GuardianRemote extends Remote {

	public StateGuardian getState() throws RemoteException;

	public void notifyRecovery() throws RemoteException;

	public void notifyWarning() throws RemoteException;

}