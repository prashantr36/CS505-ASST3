package project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class RMIServer {
	private static ServerSocket serverSocket;
    private static final String PORT_FILE = "port";
    static Semaphore semaphore;
	final static Logger log = Logger.getLogger(RMIServer.class);
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
		fa.setFile("log/_rmi_server.log");
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
	public RMIServer(String hostname, Integer portNumber) throws Exception
	{		
		// create the registry 
		RMIServerInterface rmiMethods = new RMIServerInterfaceImpl(portNumber);
		RMIServerInterfaceImpl rmi = (RMIServerInterfaceImpl) rmiMethods;
		rmi.startBackGroundFolderThread();
		LocateRegistry.createRegistry(portNumber);
		//bind the method to this name so the client can search for it
		String bindMe = "rmi://" + hostname + ":" + portNumber +  "/Calls";
		Naming.bind(bindMe, rmiMethods);
		System.out.println("RMIServer started successfully");
	}
	public static void main(String args[]) throws InterruptedException
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
			RMIServer rmi_server = new RMIServer(args[0],portNumber);
			
		}
		catch(Exception e)
		{
			log.error("RMI server binding failed with " + e.getMessage());
			e.printStackTrace();
		}
		 // create server socket
        try {
            serverSocket = new ServerSocket(portNumber + 1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // initialize the semaphore
        semaphore = new Semaphore(1);

        // server listens on its main socket and accepts client connections
        List<Thread> tlist = new ArrayList<>();
        while(true) {
            try {
                // accept client connections
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new ServerThread(socket));
                tlist.add(t);
                t.start();

            } catch (Exception e) {

                // wait for ongoing file exchange to complete
                for (Thread t: tlist) {
                    t.join();
                }
                System.out.println("server main thread exited");
                return;
            }
        }
	}

}
