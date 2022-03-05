package project3.pa2;

import java.rmi.RemoteException;

import project3.pa1.RMICoordinatorInterfaceImpl;
import project3.pa1.RMIServerInterfaceImpl;

public class LeafNodeServerInterfaceImpl extends RMIServerInterfaceImpl{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected LeafNodeServerInterfaceImpl(int portNumber) throws RemoteException {
		super(portNumber);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String QUERY_HIT_MESSAGE(String string, String key, Object message) throws RemoteException{
		return "UNIMPLEMENTED";
	}
}