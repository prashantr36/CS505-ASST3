package project3.pa2;

import java.rmi.RemoteException;

import project3.pa1.CentralIndexingServerInterface;
import project3.pa1.RMIServerInterface;

public interface SuperPeerServerInterface extends CentralIndexingServerInterface {
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException;
	public String PUSH_MESSAGE(String clientId, Object message) throws RemoteException;
}
