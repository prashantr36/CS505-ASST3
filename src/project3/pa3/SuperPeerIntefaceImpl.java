package project3.pa3;

import java.rmi.RemoteException;
import java.util.Arrays;

import project3.pa1.RMICoordinatorInterfaceImpl;
import project3.pa1.RMIClient.RMIMetadata;

public class SuperPeerIntefaceImpl extends RMICoordinatorInterfaceImpl implements SuperPeerServerInterface {

	private static final long serialVersionUID = 1L;
	private RMISuperPeerClient rmi_super_peer_client;
	protected SuperPeerIntefaceImpl(int portNumber) throws RemoteException {
		super(portNumber);
		this.rmi_super_peer_client = new RMISuperPeerClient(hostname, "" +port);
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
		for(String[] coordinator_connects: this.coordinator_connections) {
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
}
