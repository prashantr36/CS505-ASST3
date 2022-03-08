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
public interface PeerInterface extends Remote{
	public String RETRIEVE(String filename) throws RemoteException;
}