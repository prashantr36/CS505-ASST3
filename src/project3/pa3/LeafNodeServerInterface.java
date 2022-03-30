package project3.pa3;

import java.rmi.RemoteException;

import project3.pa1.RMIServerInterface;
import project3.pa3.FileRepository.FileRepositoryFile;

public interface LeafNodeServerInterface extends RMIServerInterface{
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException;
	public String QUERY_HIT_MESSAGE(String string, String key, Object message) throws RemoteException;
	public String OBTAIN(String clientId, String filename)throws RemoteException;
	public String INVALIDATION(String message_id, String clientID, String filename, int versionNumber) throws RemoteException;
	public String POLL(String clientId, String key, Long version) throws RemoteException;
	FileRepositoryFile POLL_FILE_REPOSITORY(String clientId, String key) throws RemoteException;
}
