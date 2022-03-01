package project.pa1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class RMICoordinator {
	final static Logger log = Logger.getLogger(RMICoordinator.class);
	final static String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	static void configureLogger()
	{
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.ALL);
		console.activateOptions();
		//add appender to any Logger (here is root)
		log.addAppender(console);

		// This is for the tcp_client log file
		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile("log/_rmi_coordinator.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		//add appender to any Logger (here is root)
		log.addAppender(fa);
		log.setAdditivity(false);
		//repeat with all other desired appenders
	}
	// Takes a port number to initialize the server
	public RMICoordinator(String hostname, Integer portNumber) throws Exception
	{		
		// create the registry 
		CentralIndexingServerInterface rmiMethods = new RMICoordinatorInterfaceImpl(portNumber);
		LocateRegistry.createRegistry(portNumber);
		//bind the method to this name so the client can search for it
		String bindMe = "rmi://" + hostname + ":" + portNumber + "/Calls";
		Naming.bind(bindMe, rmiMethods);
		System.out.println("RMICoordinator started successfully");
	}
	public static void main(String args[])
	{
		configureLogger();
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
				log.fatal("Fatal error : Usage - host port#" );
				System.exit(-1);
			}
		}
		Integer portNumber =0;
		try{
			portNumber = Integer.parseInt(args[1].trim());
		}
		catch(Exception e)
		{
			log.fatal(e.getMessage());
			System.exit(-1);
		}
		try{
			new RMICoordinator(args[0],portNumber);
		}
		catch(Exception e)
		{
			log.error("RMI coordinator binding failed with " + e.getMessage());
		}
	}

}
