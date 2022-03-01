package project.pa1;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;

import java.nio.file.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import project.io.RMIInputStream;
import project.io.RMIInputStreamImpl;
import project.io.RMIOutputStream;
import project.io.RMIOutputStreamImpl;

@SuppressWarnings("serial")
public class RMIServerInterfaceImpl extends UnicastRemoteObject implements RMIServerInterface, PeerInterface{
	final static Logger log = Logger.getLogger(RMIServerInterfaceImpl.class);
	final static String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	private static HashMap<String, String> hash ;
	private static String[][] hostPorts;
	public String user_directory = "files";
	public static String local_port;
	public static String local_hostname;
	public String coordinator_port;
	public String coordinator_hostname;
	private static final String ACCEPT = "ACCEPT";
	private static final String ACK = "ACK";
	private static final String NACK = "NACK";
	private static final int numCoordinators = 1;
	final public static int BUF_SIZE = 1024 * 64;
	public Runnable updateFolder;
	protected RMIServerInterfaceImpl(int portNumber) throws RemoteException {
		super();
		initializeServer();
		local_port = "" + portNumber;
		local_hostname = "localhost";
		for(int y =0 ; y < numCoordinators; y++) {
			coordinator_port = hostPorts[y][1];
			coordinator_hostname = hostPorts[y][0];
		}
		Runnable myRunnable = new FileWatcherDaemon(this);
		this.updateFolder = myRunnable;
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
	
	protected void startBackGroundFolderThread() {
		new Thread(this.updateFolder).start();
	}
	
	/*
	 * Setup server when constructor is called
	 */
	protected static void initializeServer()
	{
		configureLogger();
		hash = new HashMap<String, String>();
		hostPorts= RMIClient.readConfigFile();
	}
	/*
	 * (non-Javadoc)
	 * @see project3.RMIServerInterface#PUT(boolean, java.lang.String, java.lang.String, java.lang.String)
	 * Two phase commit for PUT operation. Only if go is true. we would commit to disk.
	 */
	public String PUT(String clientId, String key, String value)
	{	
		log.info("Server at " + local_hostname + ":" + local_port + " "+ "received [PUT " + key +"|"+value.trim() + "] from client " + clientId);
		return TwoPCommit("PUT", key, value) ? ACK : NACK;
	}
	
	/*
	 * (non-Javadoc)
	 * @see project3.RMIServerInterface#PUT(boolean, java.lang.String, java.lang.String, java.lang.String)
	 * Two phase commit for PUT operation. Only if go is true. we would commit to disk.
	 */
	public  String DELETE_X(String clientId, String key, String value)
	{	
		log.info("Server at " + local_hostname + ":" + local_port + " "+ "received [DELETE-X " + key +"|"+value.trim() + "] from client " + clientId);
		return TwoPCommit("DELETE-X", key, value) ? ACK : NACK;
	}
	
	/*
	 * Implementation of 2Phase Commit first make request to all other replicas.
	 * If "ACCEPT" is received from all the replicas then proceed to second phase, which is to commit changes.
	 * Used by the PUT and DELETE methods as they are the only ones that make write operations.
	 */
	public  boolean TwoPCommit(String command, String key, String value)
	{
		int accept = 0;
		String result = "";
		String host = "";
		int portNumber =0;
		// ASK proxies at other servers in cluster
		for(int a = 0; a<numCoordinators; a++)
		{
			result = "";
			try {
				host = InetAddress.getByName(hostPorts[a][0]).getHostAddress();
				portNumber = Integer.parseInt(hostPorts[a][1]);
				LocateRegistry.getRegistry(coordinator_hostname, portNumber);
				RMIServerInterface remoteImpl = (RMIServerInterface) Naming.lookup("rmi://" + host.trim() + ":" + portNumber + "/Calls" );
				long startTime = System.currentTimeMillis(); //Timeout after 10s
				while(result.isEmpty())
				{
					if((System.currentTimeMillis()-startTime)>10000)
					{
						log.error("Unable to ASK " + host + ":" + portNumber + "Timed-out!");
						break;
					}
					result = remoteImpl.ASK(command,local_hostname+":"+local_port, key, value);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Connection to "+ host + ":" + portNumber + " met error with "+ e.getMessage());
			}
			log.info(result+ " for " + host+ ":"+ portNumber);
			if(result.trim().toUpperCase().equals(ACCEPT))
				++accept;
			}
			if(accept == numCoordinators) {
				return true;
			}
		return false;
	}

	public  String GET(String clientId, final String key)
	{
		log.info("Server at " + local_hostname + ":" + local_port + " "+ "received [GET " + key + "] from client " + clientId);
		String response = "";
		try {
			LocateRegistry.getRegistry(coordinator_hostname, Integer.parseInt(coordinator_port));
			RMIServerInterface hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls" );
			final String peer_to_contact = hostImpl.ASK("GET", clientId, key, "");
			log.info("GET: key peer_to_contact  " + peer_to_contact);
			if(peer_to_contact.isEmpty() || peer_to_contact.contains("REJECT")) {
				log.info("No such file on any peer");
				return "No such file on any peer";
			}
			if(local_hostname.equalsIgnoreCase(peer_to_contact.split(":")[0])
					&& local_port.equalsIgnoreCase(peer_to_contact.split(":")[1])) {
				log.info("[clientId: " + clientId + "Freshest copy of " + key + " already. No GET allowed.]");
				return peer_to_contact;
			}
			if(peer_to_contact.split(":")[0].isEmpty() || peer_to_contact.split(":")[1].isEmpty()) {
				return NACK;
			}
			Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							 Downloader downloader = new Downloader(peer_to_contact.split(":")[0], Integer.parseInt(peer_to_contact.split(":")[1]) + 1, key);
					         downloader.start();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}});
			thread.start();
			response = "File " + key + " Downloaded at " + clientId;
		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			response = "No key "+ key + " matches db ";
			log.error(e.getMessage());
		}
		return response;
	}
	public  String DELETE(boolean go, String clientId, String key)
	{
		log.info("Server at " + local_hostname + ":" + local_port + " "+ "received [DELETE " + key + "] from client " + clientId);
		return TwoPCommit("DELETE", key, "") ? ACK : NACK;
	}
    
    /**
     * Copy the input stream to the output stream
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public  void copy(InputStream in, OutputStream out) 
            throws IOException {
    	
    	byte[] b = new byte[BUF_SIZE];
        int len;
        while ((len = in.read(b)) >= 0) {
        	out.write(b, 0, len);
        }
        in.close();
        out.close();
    }
    

	public OutputStream getOutputStream(File f) throws IOException {
		System.out.println("Upload file: " + f.getName());
	    return new RMIOutputStream(new RMIOutputStreamImpl(new FileOutputStream(f)));
	}
	
	public InputStream getInputStream(File f) throws IOException {
		FileInputStream file = null;
		try {
			file = new FileInputStream(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return new RMIInputStream(new RMIInputStreamImpl(file));
	}
	
    public void upload(RMIServerInterface server, File src, File dest) throws IOException {
        copy (new FileInputStream(src), server.getOutputStream(dest));
    }

    public   void download(RMIServerInterface server, File src, File dest) throws IOException , Exception {
        copy (server.getInputStream(src), new FileOutputStream(dest));
        log.info("[Server @ " + local_hostname + ":" + local_port + "Download file: " + src.getName());
    }
	@Override
	public  String ASK(String command, String clientId, String key, String value) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String DELETE(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String DELETE_X(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String RETRIEVE(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return GET(local_hostname + ":" + local_port, filename);
	}
}