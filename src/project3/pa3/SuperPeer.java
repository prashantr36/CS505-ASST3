package project3.pa3;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import project3.pa1.CentralIndexingServerInterface;
import project3.pa1.RMICoordinator;
import project3.pa3.SuperPeerIntefaceImpl;

public class SuperPeer extends RMICoordinator {
	public static String PUSH_CONSISTENCY_METHOD = "";
	public static Integer TTR = 450;
	public SuperPeer(String hostname, Integer portNumber) throws Exception {
		super(hostname, portNumber, (CentralIndexingServerInterface) new SuperPeerIntefaceImpl(portNumber));
		// TODO Auto-generated constructor stub
	}
	@SuppressWarnings("static-access")
	public static void initalize(String args[]) throws Exception {
		
		if(args.length != 2 )
		{
			System.out.println("Enter hostname");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			args = new String[2];
			try{
				System.out.println("Enter hostname");
				args[0] = br.readLine();
				System.out.println("Enter port");
				args[1] = br.readLine();
				}
			catch(Exception e)
			{
				System.out.println("Fatal error : Usage - host port#" );
				System.exit(-1);
			}
		}
		Integer portNumber =0;
		try{
			portNumber = Integer.parseInt(args[1].trim());
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		new SuperPeer(args[0], portNumber).initialize(args);
		
	}
	
	
	public static void main(String args[]) throws Exception{
		initalize(args);
	}	
}
