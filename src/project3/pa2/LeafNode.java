package project3.pa2;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import project3.pa1.RMIServer;

public class LeafNode extends RMIServer {
	protected static RMIServer rmi_server;
	public LeafNode(String hostname, Integer portNumber) throws Exception {
		super(hostname, portNumber, new LeafNodeServerInterfaceImpl(portNumber));
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
		rmi_server = new LeafNode(args[0], portNumber);
		rmi_server.initialize(args);
		rmi_server.log.info("HELLO ");
	}
	
	
	public static void main(String args[]) throws Exception{
		initalize(args);
	}
}