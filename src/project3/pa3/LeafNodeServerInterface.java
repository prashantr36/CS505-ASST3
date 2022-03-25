package project3.pa3;

import java.rmi.RemoteException;

import project3.pa1.RMIServerInterface;

public interface LeafNodeServerInterface extends RMIServerInterface{
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException;
	public String QUERY_HIT_MESSAGE(String string, String key, Object message) throws RemoteException;
	public String OBTAIN(String clientId, String filename)throws RemoteException;
	public String INVALIDATION(String message_id, String clientID, String filename, int versionNumber) throws RemoteException;
	
}
