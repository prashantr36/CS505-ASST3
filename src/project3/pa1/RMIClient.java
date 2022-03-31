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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import project3.pa3.InvalidateMessage;
import project3.pa3.LeafNodeServerInterface;
import project3.pa3.LeafNodeServerInterfaceImpl;
import project3.pa3.QueryMessage;
import project3.pa3.RMISuperPeerClient;
import project3.pa3.SuperPeerServerInterface;
import project3.pa3.FileRepository;
import project3.pa3.FileRepository.FileRepositoryFile;

public abstract class RMIClient {
	final static Logger log = Logger.getLogger(RMIClient.class);
	final static String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	final static int maxNumReplicas = 5000;
	final static int hostPortColumn = 2;
	static String[][] hostPorts;
	static LinkedHashMap<Integer, Integer> super_peer_indices;
	static HashMap<Integer, List<Integer>> leaf_node_indices;

	public enum TOPOLOGY_TYPE {
		ALL_TO_ALL, LINEAR, STAR;
	}

	static TOPOLOGY_TYPE TOPOLOGY_SELECTION;

	static void configureLogger() {
		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.ALL);
		console.activateOptions();
		// add appender to any Logger (here is root)
		log.addAppender(console);

		// This is for the tcp_client log file
		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile("log/_rmi_client.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		// add appender to any Logger (here is root)
		log.addAppender(fa);
		log.setAdditivity(false);
		// repeat with all other desired appenders
	}

	public static int getNumReplicas() {
		return maxNumReplicas;
	}

	public static class RMIMetadata {
		public String src_hostname;
		public String src_port;
		public String dst_hostname;
		public String dst_port;

		public RMIMetadata(String src_hostname, String src_port, String dst_hostname, String dst_port) {
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

	public static String[][] readConfigFile() {
		hostPorts = new String[maxNumReplicas][hostPortColumn];
		super_peer_indices = new LinkedHashMap<Integer, Integer>();
		leaf_node_indices = new HashMap<Integer, List<Integer>>();

		try {
			FileReader fReader = new FileReader("../../configuration/configs.txt");
			BufferedReader fileReader = new BufferedReader(fReader);
			int c = 0;
			int super_peer_index = 0;
			int leaf_node_index = 0;

			String str = "";
			while ((str = fileReader.readLine()) != null) {
				String splits[] = str.split("\\s+");
				if (splits.length != 2) {
					if (splits[0].contains("Gnutella")) {
						super_peer_indices.putIfAbsent(super_peer_index + 1, new Integer(-1));
						super_peer_index += 1;
						leaf_node_indices.putIfAbsent(leaf_node_index + 1, new LinkedList<Integer>());
						leaf_node_index += 1;
					}
					if (splits[0].contains("Topology")) {
						if (splits[2].contains("linear")) {
							TOPOLOGY_SELECTION = TOPOLOGY_TYPE.LINEAR;
						} else if (splits[2].contains("star")) {
							TOPOLOGY_SELECTION = TOPOLOGY_TYPE.STAR;
						} else if (splits[2].contains("all_to_all")) {
							TOPOLOGY_SELECTION = TOPOLOGY_TYPE.ALL_TO_ALL;
						}
					}
					continue;
				} else {
					if (splits[0].isEmpty() || !splits[1].matches("[0-9]+") || splits[1].isEmpty())
						continue;

					c++;
					hostPorts[c - 1][0] = splits[0];
					hostPorts[c - 1][1] = splits[1];

				}
				if (hostPorts[c - 1][0].isEmpty() || !hostPorts[c - 1][1].matches("[0-9]+")
						|| hostPorts[c - 1][1].isEmpty()) {
				} else {
					if (super_peer_indices.containsKey(super_peer_index)
							&& super_peer_indices.get(super_peer_index) == -1) {
						super_peer_indices.put(super_peer_index, c - 1);
					} else if (leaf_node_indices.containsKey(leaf_node_index)) {
						leaf_node_indices.get(leaf_node_index).add(c - 1);
					}
				}
			}
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		return hostPorts;
	}

	public static void main(String args[], String clientIdStr, String serverNumStr) {
		configureLogger();
		String clientId = "NA";
		// 4 arguments must be passed in.
		String command = "";
		String key = "";
		String values = "";
		int serverNum = 0;
		if (args.length < 2) {
			log.fatal("Fatal error : instruction key value");
			System.exit(-1);
		} else {
			readConfigFile();
			command = args[0];
			key = args[1];
			for (int a = 2; a < args.length; a++)
				values += " " + args[a];
			clientId = clientIdStr;
			serverNum = Integer.parseInt(serverNumStr);
		}
		int client_id = Integer.parseInt(clientId);
		String coordinator_hostname = hostPorts[super_peer_indices.get(client_id-1)][0];
		String coordinator_port = hostPorts[super_peer_indices.get(client_id-1)][1];

		String hostname = RMIClient.hostPorts[leaf_node_indices.get(client_id-1).get(serverNum - 1)][0];
		String port = RMIClient.hostPorts[leaf_node_indices.get(client_id-1).get(serverNum - 1)][1];
		
		forwarder(new RMIMetadata( coordinator_hostname, coordinator_port, hostname, port), key, values, serverNum,
			clientId, command, null);
	}

	public static void forwarder(RMIMetadata rmiMetadata, String key, String values, int serverNum, String clientId,
			String command, Object message) {
		try {
			configureLogger();
			String coordinator_hostname = rmiMetadata.dst_hostname;
			String coordinator_port = rmiMetadata.dst_port;
			
			String hostname = rmiMetadata.src_hostname;
			String port = rmiMetadata.src_port;
			
			// locate the remote object initialize the proxy using the binder
			int dest_server_index = 0;
			for (int t = 0; t < hostPorts.length; t++) {
				if (coordinator_hostname.equalsIgnoreCase(hostPorts[t][0]) && (coordinator_port + "").equalsIgnoreCase(hostPorts[t][1])) {
					dest_server_index = t;
					break;
				}
			}
			// Is the destination a coordinator or is a peer
			RMIServerInterface hostImpl = null;
			CentralIndexingServerInterface coordinatorHostImpl = null;
			boolean is_destination_coordinator = false;
			Integer dest_super_peer_index_key = null;
			if (super_peer_indices.values().contains(dest_server_index)) {
				for(Integer sp_key: super_peer_indices.keySet()) {
					if(super_peer_indices.get(sp_key).equals(dest_server_index)) {
						dest_super_peer_index_key = sp_key;
						break;
					}
				}
				is_destination_coordinator = true;
				coordinatorHostImpl = (CentralIndexingServerInterface) Naming
						.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls");
			} else {
				hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls");
			}

			// call the corresponding methods
			key = key.trim();
			values = values.trim();
			
			switch (command.trim().toUpperCase()) {
			/*--------- start change ----------*/
			case "DELETE":
				Path path_exists_check = Paths.get("../../Gnutella-" + (Integer.parseInt(clientId)-1) + "/RMIServer" + (serverNum) + "/files/" + key);
				if (Files.exists(path_exists_check)) {
					Files.deleteIfExists(path_exists_check);
					coordinatorHostImpl = (CentralIndexingServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
					log.info("Client on Server #" + serverNum + " RUNNING DEREGISTER :"
							+ coordinatorHostImpl.DEREGISTER(hostname + ":" + port, key));
					log.info("Client on Server #" + serverNum + " RUNNING DELETE "
							+ hostImpl.DELETE(hostname + ":" + port, key));
					String psb = System.getenv("PUSH_BASED_CONSISTENCY").trim();
					if(psb.equalsIgnoreCase("TRUE"))
						((SuperPeerServerInterface) coordinatorHostImpl).INVALIDATION("" + InvalidateMessage.TYPE_ID, hostname + ":" + port, key, -1);
				}
				break;
			case "EDIT":
				Path path_exists = Paths.get("../../Gnutella-" + (Integer.parseInt(clientId)-1) + "/RMIServer" + (serverNum) + "/files/" + key);
				if (Files.exists(path_exists)) {
					
					log.info(
							
							"Client on Server #" + serverNum + " RUNNING EDIT :" + hostImpl.EDIT(coordinator_hostname + ":" + coordinator_port, key));
					
					String psb = System.getenv("PUSH_BASED_CONSISTENCY").trim();
					if(psb.equalsIgnoreCase("TRUE")) {
						coordinatorHostImpl = (CentralIndexingServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
						((SuperPeerServerInterface) coordinatorHostImpl).INVALIDATION("" + InvalidateMessage.TYPE_ID, hostname + ":" + port, key, FileRepository.FORCE_FILE_STALE_VERSION_NUMBER);
					}
				}
				break;
			case "INVALIDATE_MESSAGE":
				String psb = System.getenv("PUSH_BASED_CONSISTENCY").trim();
				int ttr = Integer.parseInt(System.getenv("TTR").trim());
				
				if(psb.equalsIgnoreCase("TRUE")) {
					if (message instanceof Serializable && is_destination_coordinator) {
						InvalidateMessage i_message = (InvalidateMessage) message;
						for(Integer leaf_hostport_key: leaf_node_indices.get(dest_super_peer_index_key)) {
							i_message = (InvalidateMessage) message;
							hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + 
							(hostPorts[leaf_hostport_key][0]) + ":" + (hostPorts[leaf_hostport_key][1]) + "/Calls");
							((LeafNodeServerInterface) hostImpl).INVALIDATION("" + i_message.TYPE_ID,
									hostname + ":" + port, i_message.getKey(),
									i_message.getVersionNumber());
						}
					}
					else {
						log.error(" <- PUSH BASED CONSISTENCY ->" + psb);
							log.error("Error Client on Server #" + rmiMetadata + " RUNNING INVALIDATE MESSAGE: "
									+ " garbled message.");
					}
				}
				/*--------- end change ----------*/
				break;
			case "GET":
				hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls" );
				log.info(
						"Client on Server #" + serverNum + " RUNNING GET :" + hostImpl.GET(coordinator_hostname + ":" + coordinator_port, key));
				break;
			case "RETRIEVE":
				hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls" );
				log.info("Client on Server #" + serverNum + " RUNNING RETRIEVE :"
						+ hostImpl.RETRIEVE(coordinator_hostname + ":" + coordinator_port, key));
				break;
			case "REFRESH":
				hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + coordinator_hostname + ":" + coordinator_port + "/Calls" );
				Thread.sleep(3000);
				log.info("Client on Server #" + serverNum + " RUNNING REFRESH :"
						+ hostImpl.RETRIEVE(coordinator_hostname + ":" + coordinator_port, key));
				break;
			case "OBTAIN":
					hostImpl.OBTAIN(hostname + ":" + port, key);
				break;
			case "SEARCH":
				coordinatorHostImpl = (CentralIndexingServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
				log.info("Client on Server #" + serverNum + " RUNNING SEARCH :" + coordinatorHostImpl.SEARCH(key));
				break;
			case "REGISTRY":
				coordinatorHostImpl = (CentralIndexingServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
				Path path = Paths.get("../../Gnutella-" + (Integer.parseInt(clientId)-1) + "/RMIServer" + (serverNum) + "/files/" + key);
				if (Files.exists(path)) {
					log.info("Client on Server #" + serverNum + " RUNNING REGISTRY :"
							+ coordinatorHostImpl.REGISTRY(hostname + ":" + port, key));
				} else {
					log.error("Error: Client on Server #" + serverNum + " RUNNING REGISTRY cause:"
							+ " no such file on server" + path.toAbsolutePath().toString());
				}

				break;
			case "DEREGISTER":
				coordinatorHostImpl = (CentralIndexingServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
				log.info("Client on Server #" + serverNum + " RUNNING DEREGISTER :"
						+ coordinatorHostImpl.DEREGISTER(hostname + ":" + port, key));
				break;
			case "PUT":
				hostImpl = (RMIServerInterface) Naming.lookup("rmi://" + hostname + ":" + port + "/Calls" );
				log.info("Client on Server #" + serverNum + " RUNNING PUT "
						+ hostImpl.PUT(hostname + ":" + port, key, values));
				break;	
			case "QUERY_MESSAGE":
				if (message instanceof Serializable) {
					if (is_destination_coordinator)
						coordinatorHostImpl.QUERY_MESSAGE(hostname + ":" + port, message);
				} else {
					if (!is_destination_coordinator) {
						QueryMessage q_message = new QueryMessage(key);
						hostImpl.QUERY_MESSAGE(hostname + ":" + port, q_message);
					} else {
						log.error("Error Client on Server #" + rmiMetadata + " RUNNING QUERY MESSAGE: "
								+ " garbled message.");
					}
				}
				break;
			case "QUERY_HIT_MESSAGE":
				if (message instanceof Serializable)
					if (is_destination_coordinator)
						coordinatorHostImpl.QUERY_HIT_MESSAGE(hostname + ":" + port, key, message);
					else {
						hostImpl.QUERY_HIT_MESSAGE(hostname + ":" + port, key, message);
					}
				else {
					log.error(
							"Error Client on Server #" + rmiMetadata + " RUNNING QUERY_HIT MESSAGE: " + " garbled message."
					+ " KEY " + key + " Command " + command + " message " + message);
				}
				break;

			default:
				String response = "Client " + clientId + ":" + "Invalid command " + command + " was received";
				log.error(response);
				break;
			}
		} catch (Exception e) {
			log.error("Error occured while connecting to RMI server with error, ");
			e.printStackTrace();
		}
	}

}
