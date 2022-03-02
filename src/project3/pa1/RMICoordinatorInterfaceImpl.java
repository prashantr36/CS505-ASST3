package project3.pa1;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import project3.io.RMIInputStream;
import project3.io.RMIInputStreamImpl;
import project3.io.RMIOutputStream;
import project3.io.RMIOutputStreamImpl;

@SuppressWarnings("serial")
public class RMICoordinatorInterfaceImpl extends UnicastRemoteObject implements RMIServerInterface, CentralIndexingServerInterface{
	final static Logger log = Logger.getLogger(RMIServerInterfaceImpl.class);
	final static String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	private static ConcurrentHashMap<String, LinkedHashSet<String>> hash ;
	private static String[][] hostPorts;
	public int port;
	public String hostname;
	private static final String ACCEPT = "ACCEPT";
	private static final String ACK = "ACK";
	private static final String NACK = "NACK";
	private static final int numCoordinators = 1;
	final public static int BUF_SIZE = 1024 * 64;
	protected RMICoordinatorInterfaceImpl(int portNumber) throws RemoteException {
		super();
		initializeServer();
		port = portNumber;
		hostname = "localhost";
		
	}
	/* Configure the log4j appenders
	 */
	static void configureLogger()
	{
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.ALL);
		console.activateOptions();
		//add appender to any Logger (here is root)
		log.addAppender(console);
		// This is for the rmi_server log file
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
	/*
	 * Setup server when constructor is called
	 */
	protected static void initializeServer()
	{
		configureLogger();
		hash = new ConcurrentHashMap<String, LinkedHashSet<String>>();
		hostPorts= RMIClient.readConfigFile();
	}
	
	/*
	 * (non-Javadoc)
	 * @see project3.RMIServerInterface#ASK(java.lang.String, java.lang.String, java.lang.String)
	 * ASK method checks with the replica that the command being requested with the key and value is legitimate and acceptable to the server.
	 * If the value is not being modified for the key passed in, then reject.
	 * Similarly, if the key doesn't exist in the replica, then ASK would reject for this replica.
	 * Enforces consistency of data in the absence of failures.  
	 */
	@Override
	public synchronized String ASK(String command, String clientId, String key, String value)
	{
			log.info("COORDINATOR HASH " + hash);
			log.info("---");
			log.info(" TRYING TO FIND KEY " + hash.containsKey(key) + " key " + key +  " " + clientId);
			if(hash.containsKey(key))
			{
				if(command.toUpperCase().equals("PUT")) {
					hash.get(key).add(clientId);
					return "ACCEPT";
				}
				else
					if(command.toUpperCase().equals("DELETE")) {
						
						if (hash.containsKey(key) 
								&& hash.get(key) != null
									&& !hash.get(key).isEmpty()) {
							hash.get(key).remove(clientId);
						}
						
					} else if(command.toUpperCase().equals("GET")) {
						if(hash.get(key).isEmpty()) {
							return "REJECT";
						} else {
							return hash.get(key).iterator().next();
						}
					}
					
					return "ACCEPT";
			}
			else{
				if(command.toUpperCase().equals("PUT")) {
					 hash.putIfAbsent(key, new LinkedHashSet<String>());
					 hash.get(key).add(clientId);
					 return "ACCEPT";
				}
				else
					return "REJECT";
			}
	}
	@Override
	public String PUT(String clientId, String key, String value) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String GET(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String DELETE(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public OutputStream getOutputStream(File f) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public InputStream getInputStream(File f) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String DELETE_X(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String SEARCH(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return ASK("GET", hostname+":"+port, filename, "");
	}
	@Override
	public String DEREGISTER(String clientId, String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return ASK("DELETE", clientId, filename, "");
	}
	@Override
	public String REGISTRY(String clientId, String filename) throws RemoteException {
		return ASK("PUT", clientId, filename, "");
	}
}
