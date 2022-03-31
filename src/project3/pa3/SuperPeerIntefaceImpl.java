package project3.pa3;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import project3.pa1.RMICoordinatorInterfaceImpl;
import project3.pa1.RMIServerInterface;
import project3.pa3.FileRepository.FileRepositoryFile;
import project3.pa1.RMIClient.RMIMetadata;

public class SuperPeerIntefaceImpl extends RMICoordinatorInterfaceImpl implements SuperPeerServerInterface {
	
	/*--------- start change ----------*/
	private static final long serialVersionUID = 1L;
	private RMISuperPeerClient rmi_super_peer_client;
	private HashMapHitCounter hash_map_hit_counter;
	protected SuperPeerIntefaceImpl(int portNumber) throws RemoteException {
		super(portNumber);
		this.rmi_super_peer_client = new RMISuperPeerClient(hostname, "" +port);
		this.hash_map_hit_counter = new HashMapHitCounter();
	}
	
	public class HashMapHitCounter {
		public HashMap<Message, List<String>> file_repositories;
		public HashMap<Message, Integer> hit_counter;
		public HashMapHitCounter() {
			this.file_repositories = new HashMap<Message, List<String>>();
			this.hit_counter = new HashMap<Message, Integer>();
		}
		
		public boolean contains(Message key) {
			return this.file_repositories.containsKey(key);
		}
		
		public void forceAdd(Message key) {
			this.file_repositories.putIfAbsent(key, new ArrayList<String>());
			this.hit_counter.putIfAbsent(key, 0);
		}
		public void put(Message key, String [] values) {
			this.hit_counter.computeIfPresent(key, (k, v) -> v + 1);
			Collections.addAll(file_repositories.get(key), values);
		}
	}
	public HashMap<String, FileRepositoryFile> SEARCH(String clientId, String filename) throws RemoteException {
		QueryMessage query = new QueryMessage(filename);

		hash_map_hit_counter.forceAdd(query);
		try {
          query.decrementTimeToLiveCounter();
          for(String[] coordinator_connects: this.coordinator_connections) {
        	  RMIMetadata rm_metadata_copy = new RMIMetadata(hostname, port +"", coordinator_connects[0],
        			  											coordinator_connects[1]);
        	  rm_metadata_copy.dst_hostname = coordinator_connects[0];
        	  rm_metadata_copy.dst_port = coordinator_connects[1];
        	  RMISuperPeerClient.forward(query, clientId, query.key, "QUERY_MESSAGE", rm_metadata_copy);
          }
        } catch (MessageExpiredException e) {
        }
		
        String[] matches = getMatches(query.key).toArray(new String[0]);
        if(hash_map_hit_counter.contains((Message)query)) {
        	hash_map_hit_counter.put((Message)query, matches);
        }
        try {
        	TimeUnit.MILLISECONDS.sleep(400);
        } catch(InterruptedException e) {
        	
        }
        System.out.println(" ALL THE KEYS " + hash_map_hit_counter.file_repositories.get((Message) query));
        
        Set<String> available_leaf_nodes = new HashSet<String>(hash_map_hit_counter.file_repositories.get((Message) query));
        for(String [] coords: all_coordinator_connections) {
        	available_leaf_nodes.remove(coords[0] + ":" + coords[1]);
        }
        HashMap<String, FileRepositoryFile> file_repository_list = new HashMap<String, FileRepositoryFile>();
        
        for (String avail : available_leaf_nodes) {
        	try {
				LeafNodeServerInterface hostImpl = (LeafNodeServerInterface) Naming.lookup("rmi://" + avail.split(":")[0] + ":" + avail.split(":")[1] + "/Calls" );
				file_repository_list.put(avail, hostImpl.POLL_FILE_REPOSITORY(clientId, filename));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        hash_map_hit_counter.file_repositories.remove((Message) query);
        return file_repository_list;
	}
	
	@Override
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException {
	
		QueryMessage query = (QueryMessage) message;
		log.info("[SUPERPEER-QUERY]  Received Query message from " + clientId + " " + message
				+ " at " + hostname + " port " + port);
		
        if (rmi_super_peer_client.haveSeen(query)) {
        	log.info("Has been seen! Query message from " + clientId + " " + message
    				+ " at " + hostname + " port " + port);
          return "THIS MESSAGE HAS BEEN SEEN";
        } else {
        }
        if(clientId.split(":")[0].isEmpty() || clientId.split(":")[1].isEmpty()) {
			return "Error in parsing clientId" + clientId;
		}
        String dst_source = clientId.split(":")[0];
        String dst_port = clientId.split(":")[1];
        
        RMIMetadata rmi_metadata = new RMIMetadata(hostname, port +"", dst_source, dst_port);
        rmi_super_peer_client.setSeen(query, rmi_metadata);
        try {
          query.decrementTimeToLiveCounter();
          for(String[] coordinator_connects: this.coordinator_connections) {
        	  RMIMetadata rm_metadata_copy = new RMIMetadata(hostname, port +"", coordinator_connects[0],
        			  											coordinator_connects[1]);
        	  rm_metadata_copy.dst_hostname = coordinator_connects[0];
        	  rm_metadata_copy.dst_port = coordinator_connects[1];
        	  RMISuperPeerClient.forward(message, hostname + ":" + port, query.key, "QUERY_MESSAGE", rm_metadata_copy);
          }
        } catch (MessageExpiredException e) {
        }
        String[] matches = getMatches(query.key).toArray(new String[0]);
        for(String match: matches) {
        	if(clientId.equalsIgnoreCase(match)) {
        		// If request has already been fulfilled, early terminate
        		// without QUERY_HIT_MESSAGE
        		return ACK;
        	}
        }
        if (matches.length > 0) {
          QueryHitMessage hit = new QueryHitMessage(query.messageId, matches,
                  hostname, port + "");
          RMISuperPeerClient.forward(hit, hostname + ":" + port, query.key, "QUERY_HIT_MESSAGE", new RMIMetadata(hostname, port +"", rmi_metadata.dst_hostname, rmi_metadata.dst_port));
        }
        return ACK;
	}
	
	/*--------- end change ----------*/
	@Override
	public String QUERY_HIT_MESSAGE(String clientId, String key, Object message) throws RemoteException {
		QueryHitMessage query_hit_message = (QueryHitMessage) message;
		RMIMetadata conn = rmi_super_peer_client.getDestination(query_hit_message);
		log.info("[SUPERPEER-QHM] Received QUERY_HIT_MESSAGE message from " + clientId + " " + query_hit_message
				+ " at " + hostname + " port " + port);
		if (conn == null) {
          return "Invalid connection";
        }
        QueryHitMessage hit = (QueryHitMessage) message;
          try {
            query_hit_message.decrementTimeToLiveCounter();
            if(hash_map_hit_counter.contains((Message)query_hit_message)) {
            	hash_map_hit_counter.put((Message)query_hit_message, query_hit_message.matches);
            }
            RMISuperPeerClient.forward(hit, hostname + ":" + port, key, "QUERY_HIT_MESSAGE", new RMIMetadata(conn.src_hostname, conn.src_port
            												, conn.dst_hostname, conn.dst_port));
          } catch (MessageExpiredException e) {
        	  log.info(query_hit_message + " " + "Expired TTL");
          }
        return ACK;
	}

	@Override
	public String OBTAIN(String clientId, String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String INVALIDATION(String message_id, String clientId, String filename, int versionNumber)
			throws RemoteException {
		InvalidateMessage message = (InvalidateMessage) new InvalidateMessage(filename, versionNumber);
		log.info("[INVALIDATION-QUERY]  Received Query message from " + clientId + " " + message
				+ " at " + hostname + " port " + port);
		
		if(clientId.split(":")[0].isEmpty() || clientId.split(":")[1].isEmpty()) {
			return "Error in parsing clientId" + clientId;
		}
		String dst_source = clientId.split(":")[0];
		String dst_port = clientId.split(":")[1];
		
		RMIMetadata rmi_metadata = new RMIMetadata(hostname, port +"", dst_source, dst_port);
		for(String[] coordinator_connects: this.all_coordinator_connections) {
		  RMIMetadata rm_metadata_copy = new RMIMetadata(hostname, port +"", coordinator_connects[0],
			  											coordinator_connects[1]);
		  rm_metadata_copy.dst_hostname = coordinator_connects[0];
		  rm_metadata_copy.dst_port = coordinator_connects[1];
		  RMISuperPeerClient.forward(message, hostname + ":" + port, message.key, "INVALIDATE_MESSAGE", rm_metadata_copy);
		}
		return ACK;
	}

	@Override
	public String EDIT(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String RETRIEVE(String clientId, String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}
