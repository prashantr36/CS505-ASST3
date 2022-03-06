package project3.pa1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public abstract class RMIClient {
	final static Logger log = Logger.getLogger(RMIClient.class);
	final static String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	final static int maxNumReplicas = 5000;
	final static int hostPortColumn = 2;
	static HashMap<Integer, Integer> super_peer_indices;
	static HashMap<Integer, List<Integer>> leaf_node_indices;
	
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
		fa.setFile("log/_rmi_client.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		//add appender to any Logger (here is root)
		log.addAppender(fa);
		log.setAdditivity(false);
		//repeat with all other desired appenders
	}
	
	public static int getNumReplicas()
	{
		return maxNumReplicas;
	}
	
	public static class RMIMetadata {
		 public String src_hostname;
		 public String src_port;
		 public String dst_hostname;
		 public String dst_port;
		 public RMIMetadata(String src_hostname,
				 			String src_port,
				 			String dst_hostname,
				 			String dst_port) {
			 this.src_hostname = src_hostname;
			 this.src_port = src_port;
			 this.dst_hostname = dst_hostname;
			 this.dst_port = dst_port;
		 }
		public RMIMetadata() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public String toString() {
			return "RMIMetadata [src_hostname=" + src_hostname + ", src_port=" + src_port + ", dst_hostname="
					+ dst_hostname + ", dst_port=" + dst_port + "]";
		}		
	}
	
	public static String[][] readConfigFile()
	{		
		String hostPorts [][] = new String[maxNumReplicas][hostPortColumn];
		super_peer_indices = new HashMap<Integer, Integer>();
		leaf_node_indices = new HashMap<Integer, List<Integer>>();
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader("../../configuration/configs.txt"));			
			System.out.println("Loading configurations from configs.txt..");
			int c = 0;
			int super_peer_index = 0;
			int leaf_node_index = 0;
			
			String str ="";
			while((str = fileReader.readLine()) != null)
			{
				 String splits[] = str.split("\\s+");
				 if(splits.length != 2) {
					  if(splits[0].contains("Gnutella")) {
							super_peer_indices.putIfAbsent(super_peer_index + 1, new Integer(-1));
							super_peer_index += 1;
							leaf_node_indices.putIfAbsent(leaf_node_index + 1, new LinkedList<Integer>());
							leaf_node_index += 1;
					 }
					 continue;
				 } else {
					 if(splits[0].isEmpty() || 
								!splits[1].matches("[0-9]+") ||
								splits[1].isEmpty())
						 continue;
					
					 c++;
					 hostPorts[c-1][0] = splits[0];
					 hostPorts[c-1][1] = splits[1];
					 
				 }
				if(hostPorts[c-1][0].isEmpty() || 
						!hostPorts[c-1][1].matches("[0-9]+") ||
						hostPorts[c-1][1].isEmpty()) {
				} else {
					if(super_peer_indices.containsKey(super_peer_index)
							&& super_peer_indices.get(super_peer_index) == -1) {
						super_peer_indices.put(super_peer_index, c-1);
					} else if(leaf_node_indices.containsKey(leaf_node_index)) {
						leaf_node_indices.get(leaf_node_index).add(c-1);
					}
				}
			}
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("System exited with error " +e.getMessage());
			System.exit(-1);
		}
		return hostPorts;
	}
	
	public static void main(String args[])
	{
		configureLogger();
		String clientId = "NA";
		// 4 arguments must be passed in.
		String hostPorts [][] = new String[maxNumReplicas][hostPortColumn];
		String command = "";
		String key = "";
		String values = "";		
		int serverNum =0;
		if(args.length < 2  )
		{
			log.fatal("Fatal error : instruction key value" );
			System.exit(-1);
		}
		else
		{
			hostPorts = readConfigFile();
			command = args[0];
			key = args[1];
			for(int a = 2; a <args.length; a++)
				values +=" " + args[a];
			clientId = System.getProperty("clientId");
			serverNum = Integer.parseInt(System.getProperty("serverChoice"));
		}
		
		int client_id = Integer.parseInt(clientId);
		
		
		//System.out.println(" super_peer_indices " + super_peer_indices);
		//System.out.println(" leaves " + leaf_node_indices);
		
		
		
		String coordinator_hostname = hostPorts[super_peer_indices.get(client_id)][0];
		String coordinator_port = hostPorts[super_peer_indices.get(client_id)][1];
		
		String hostname = hostPorts[leaf_node_indices.get(client_id).get(serverNum-1)][0];
		String port = hostPorts[leaf_node_indices.get(client_id).get(serverNum-1)][1];
				
		forwarder(new RMIMetadata(coordinator_hostname,
				  coordinator_port, 
				  hostname,
				  port),
				  key,
				  values,
				  serverNum,
				  clientId,
				  command,
				  null);
	}
	
	public static RMIMetadata getRMIMetadat(String destination_hostname,
								String destination_port,
								String source_hostname,
								String source_port) {
		return new RMIMetadata(destination_hostname, destination_port,
					source_hostname, source_port);
	}
	
	public static void forwarder(RMIMetadata rmiMetadata,
								 String key,
								 String values, 
								 int serverNum,
								 String clientId, 
								 String command,
								 Object message) {
		try{
			String coordinator_hostname = rmiMetadata.src_hostname;
			String coordinator_port = rmiMetadata.src_port;
			String hostname = rmiMetadata.dst_hostname;
			String port = rmiMetadata.dst_port;
			
			
			log.info(" RMIMETADATA " + rmiMetadata);
			// Locate the Coordinator
			CentralIndexingServerInterface coordinatorHostImpl = (CentralIndexingServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls" );
			
			// locate the remote object initialize the proxy using the binder
			RMIServerInterface hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
			
			
			// call the corresponding methods
			key = key.trim();
			values = values.trim();
			if(System.getProperty("clientId") != null)
				clientId = System.getProperty("clientId");

			switch(command.trim().toUpperCase()){
			case "GET":
				log.info("Client on Server #" + serverNum + " RUNNING GET :" + hostImpl.GET(hostname + ":" + port,key));
				break;
			case "RETRIEVE":
				log.info("Client on Server #" + serverNum + " RUNNING RETRIEVE :" + hostImpl.GET(hostname + ":" + port,key));
				break;
			case "SEARCH":
				log.info("Client on Server #" + serverNum + " RUNNING SEARCH :" + coordinatorHostImpl.SEARCH(key));
				break;
			case "REGISTRY":
				Path path = Paths.get("../../Gnutella-" + (clientId) + "/RMIServer" + (serverNum) + "/files/" + key);
				if (Files.exists(path)) {
					log.info("Client on Server #" + serverNum + " RUNNING REGISTRY :" + coordinatorHostImpl.REGISTRY(hostname + ":" + port,key));
				} else {
					log.error("Error: Client on Server #" + serverNum + " RUNNING REGISTRY cause:" + " no such file on server");
				}
				
				break;
			case "DEREGISTER":				
				log.info("Client on Server #" + serverNum + " RUNNING DEREGISTER :" + coordinatorHostImpl.DEREGISTER(hostname + ":" + port,key));
				break;
			case "PUT":
				
				log.info("Client on Server #" + serverNum + " RUNNING GET " +  hostImpl.PUT(hostname + ":" + port,key,values));
				break;
			case "DELETE":
				log.info("Client on Server #" + serverNum + " RUNNING GET " +  hostImpl.DELETE(hostname + ":" + port,key));
				break;
			
			case "PUSH_MESSAGE":
				if(message instanceof Serializable)
					log.info("Client on Server #" + serverNum + " RUNNING PUSH MESSAGE " +  coordinatorHostImpl.PUSH_MESSAGE(hostname + ":" + port, message));
				log.error("Error Client on Server #" + serverNum + " RUNNING PUSH MESSAGE: " + " garbled message.");
				break;
			
			case "QUERY_MESSAGE":
				if(message instanceof Serializable)
					log.info("Client on Server #" + serverNum + " RUNNING QUERY MESSAGE " +  coordinatorHostImpl.QUERY_MESSAGE(hostname + ":" + port, message));
				log.error("Error Client on Server #" + serverNum + " RUNNING QUERY MESSAGE: " + " garbled message.");
				break;	
			case "QUERY_HIT_MESSAGE":
				if(message instanceof Serializable)
					log.info("Client on Server #" + serverNum + " RUNNING QUERY_HIT MESSAGE " +  hostImpl.QUERY_HIT_MESSAGE(hostname + ":" + port,key, message));
				log.error("Error Client on Server #" + serverNum + " RUNNING QUERY_HIT MESSAGE: " + " garbled message.");
				break;	
				
			default:
				String response = "Client "+clientId + ":" +  "Invalid command " + command + " was received";
				log.error(response);
				break;
			}
		}
		catch(Exception e)
		{
			log.error("Error occured while connecting to RMI server with error, " + e.getMessage());
		}
	}
	
	
}

