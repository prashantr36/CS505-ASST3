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
public interface RMIServerInterface extends Remote{
	public String PUT(String clientId, String key,String value) throws RemoteException;
	public String GET(String clientId,String key)throws RemoteException;
	public String DELETE(String clientId, String key)throws RemoteException;
	public String DELETE_X(String clientId, String key)throws RemoteException;
	public String ASK(String command, String clientId, String key, String value) throws RemoteException;
	public OutputStream getOutputStream(File f) throws IOException;
	public InputStream getInputStream(File f) throws IOException;
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException;
	public String QUERY_HIT_MESSAGE(String string, String key, Object message) throws RemoteException;
	public String OBTAIN(String clientId, String filename)throws RemoteException;
}