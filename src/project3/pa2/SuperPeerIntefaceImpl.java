package project3.pa2;

import java.rmi.RemoteException;

import project3.pa1.RMICoordinatorInterfaceImpl;

public class SuperPeerIntefaceImpl extends RMICoordinatorInterfaceImpl{

	private static final long serialVersionUID = 1L;

	protected SuperPeerIntefaceImpl(int portNumber) throws RemoteException {
		super(portNumber);
	}

	
	@Override
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException {
		// TODO Auto-generated method stub
		return "UNIMPLEMENTED";
	}

	@Override
	public String PUSH_MESSAGE(String clientId, Object message) throws RemoteException {
		return "UNIMPLEMENTED";
	}


	@Override
	public String QUERY_HIT_MESSAGE(String string, String key, Object message) throws RemoteException {
		return null;
	}

}
