package project3.pa1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 * Server interface- clients can call the following methods
 */
public interface CentralIndexingServerInterface extends Remote{
	public String REGISTRY(String clientId, String filename) throws RemoteException;
	public String SEARCH(String filename)throws RemoteException;
	public String DEREGISTER(String clientId, String filename)throws RemoteException;
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException;
	public String QUERY_HIT_MESSAGE(String clientId, String key, Object message) throws RemoteException;
}