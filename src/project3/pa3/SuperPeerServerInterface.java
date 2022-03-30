package project3.pa3;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import project3.pa1.CentralIndexingServerInterface;
import project3.pa1.RMIServerInterface;
import project3.pa3.FileRepository.FileRepositoryFile;

public interface SuperPeerServerInterface extends CentralIndexingServerInterface {
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException;
	public String QUERY_HIT_MESSAGE(String clientId, String key, Object message) throws RemoteException;
	public String INVALIDATION(String message_id, String clientId, String filename, int versionNumber) throws RemoteException;
	public HashMap<String, FileRepositoryFile> SEARCH(String clientId, String filename) throws RemoteException;
}
