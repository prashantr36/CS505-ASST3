package project3.pa1;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("serial")
public abstract class RMIServerInterfaceImpl extends UnicastRemoteObject implements RMIServerInterface, PeerInterface{
	protected final static Logger log = Logger.getLogger(RMIServerInterfaceImpl.class);
	final static String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	private static HashMap<String, String> hash ;
	private static String[][] hostPorts;
	public String user_directory = "files";
	public static String local_port;
	public static String local_hostname;
	public String coordinator_port;
	public String coordinator_hostname;
	private static final String ACCEPT = "ACCEPT";
	protected static final String ACK = "ACK";
	private static final String NACK = "NACK";
	private static final int numCoordinators = 1;
	final public static int BUF_SIZE = 1024 * 64;
	public Runnable updateFolder;
	protected List<String[]> coordinator_connections;
	protected RMIServerInterfaceImpl(int portNumber) throws Exception {
		super();
		initializeServer();
		local_port = "" + portNumber;
		local_hostname = "localhost";
		this.coordinator_hostname ="";
		for(Integer k: RMIClient.super_peer_indices.keySet()) {
			for(Integer u: RMIClient.leaf_node_indices.get(k)) {
				if(local_hostname.equalsIgnoreCase(hostPorts[u][0])
					&& local_port.equalsIgnoreCase(hostPorts[u][1])) {
					this.coordinator_hostname = hostPorts[RMIClient.super_peer_indices.get(k)][0];
					this.coordinator_port = hostPorts[RMIClient.super_peer_indices.get(k)][1];
					break;
				}
			}
			if(!coordinator_hostname.isEmpty()) {
				break;
			}
		}
		this.coordinator_connections = new ArrayList<String[]>();
		this.coordinator_connections.add(new String[] {coordinator_hostname, coordinator_port});
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
	
	protected void startBackGroundFolderThread() throws IOException {
		System.out.println("STARTED BACKGROUND THREAD");
		Files.createDirectories(Paths.get("files"));
		new Thread(this.updateFolder).start();
		Files.createDirectories(Paths.get("files"));
	}
	
	/*
	 * Setup server when constructor is called
	 */
	protected static void initializeServer() throws IOException
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
		Integer portNumber =0;
		// ASK proxies at other servers in cluster
		for(int a = 0; a<numCoordinators; a++)
		{
			result = "";
			try {
				host = coordinator_hostname;
				portNumber = Integer.parseInt(coordinator_port);
				System.out.println(" COORDINATOR_HOSTNAME " + coordinator_hostname
							+ " coordinator_port " + portNumber);
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
			

				CompletableFuture
		            .supplyAsync(() -> {
		            	 try {
			            	 Downloader downloader = new Downloader(peer_to_contact.split(":")[0], Integer.parseInt(peer_to_contact.split(":")[1]) + 1, key);
					         downloader.start();
					         
		            	 } catch(Exception e) {
		            		 throw new RuntimeException("socket closed unexpectedly or file not found.");   
		            	 }
		            	 try {
							hostImpl.ASK("PUT", clientId, key, "");
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            	 return "File " + key + " Downloaded at " + clientId;
		            }).exceptionally(ex -> {
		            	try {
							hostImpl.ASK("DELETE", clientId, key, "");
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            return "ERR: File cannot be downloaded exception"  + ex;
			        }).thenApplyAsync(input -> input)
				       .thenAccept(log::info);
							
			response = "File " + key + " Downloaded at " + clientId;
		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			response = "No key "+ key + " matches db ";
			log.error(e.getMessage());
		}
		return response;
	}
	
	
	public  String OBTAIN(String clientId, final String key) throws RemoteException
	{
		log.info("Server at " + local_hostname + ":" + local_port + " "+ "received [OBTAIN " + key + "] from client " + clientId);
		String response = "";
		try {
			LocateRegistry.getRegistry(coordinator_hostname, Integer.parseInt(coordinator_port));
		
			RMIServerInterface hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls" );
		
			final String peer_to_contact = clientId;
			CompletableFuture
	            .supplyAsync(() -> {
	            	 try {
		            	 Downloader downloader = new Downloader(peer_to_contact.split(":")[0], Integer.parseInt(peer_to_contact.split(":")[1]) + 1, key);
				         downloader.start();
				         
	            	 } catch(Exception e) {
	            		 throw new RuntimeException("socket closed unexpectedly or file not found.");   
	            	 }
	            	 try {
						hostImpl.ASK("PUT", clientId, key, "");
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	 return "File " + key + " Downloaded at " + clientId;
	            }).exceptionally(ex -> {
	            	try {
						hostImpl.ASK("DELETE", clientId, key, "");
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            return "ERR: File cannot be downloaded exception"  + ex;
		        }).thenApplyAsync(input -> input)
			       .thenAccept(log::info);
						
				response = "File " + key + " Downloaded at " + clientId;}
				catch(Exception e) {
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
	
	@Override
	public String OBTAIN(String filename) throws RemoteException{
		// TODO Auto-generated method stub
		return OBTAIN(local_hostname + ":" + local_port, filename);
	}
	
	@Override
	public String QUERY_HIT_MESSAGE(String string, String key, Object message)  throws RemoteException {
		return null;
	}
	@Override
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException {
		return null;
	}
}
