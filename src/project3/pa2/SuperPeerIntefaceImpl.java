package project3.pa2;

import java.rmi.RemoteException;
import java.util.Arrays;

import project3.pa1.RMICoordinatorInterfaceImpl;
import project3.pa1.RMIClient.RMIMetadata;

public class SuperPeerIntefaceImpl extends RMICoordinatorInterfaceImpl{

	private static final long serialVersionUID = 1L;
	private RMISuperPeerClient rmi_super_peer_client;
	protected SuperPeerIntefaceImpl(int portNumber) throws RemoteException {
		super(portNumber);
		this.rmi_super_peer_client = new RMISuperPeerClient(hostname, "" +port);
	}
	
	@Override
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException {
	
		QueryMessage query = (QueryMessage) message;
		System.out.println("Received Query message from " + clientId + " " + message
				+ " at " + hostname + " port " + port);
        if (rmi_super_peer_client.haveSeen(query)) {
        	System.out.println("Has been seen! Query message from " + clientId + " " + message
    				+ " at " + hostname + " port " + port);
          return "THIS MESSAGE HAS BEEN SEEN";
        } else {
        }
        if(clientId.split(":")[0].isEmpty() || clientId.split(":")[1].isEmpty()) {
			return "Error in parsing clientId" + clientId;
		}
        String dst_source = clientId.split(":")[0];
        String dst_port = clientId.split(":")[1];
        
        RMIMetadata rmi_metadata = new RMIMetadata(dst_source, dst_port, hostname, port +"");
        rmi_super_peer_client.setSeen(query, rmi_metadata);
        System.out.println("CAME HERE 1");
        try {
          query.prepareForward();
          for(String[] coordinator_connects: this.coordinator_connections) {
        	  rmi_metadata.dst_hostname = coordinator_connects[0];
        	  rmi_metadata.dst_port = coordinator_connects[1];
        	  RMISuperPeerClient.forward(message, query.key, "QUERY_MESSAGE", rmi_metadata);
          }
        } catch (MessageExpiredException e) {
        }
        System.out.println("CAME HERE 2 ");
        String[] matches = getMatches(query.key).toArray(new String[0]);
        System.out.println(" MATCHES FOUND " + Arrays.toString(matches) + " FOR KEY " + query.key);
        if (matches.length > 0) {
          QueryHitMessage hit = new QueryHitMessage(query.messageId, matches,
                  hostname, port + "");
          RMISuperPeerClient.forward(hit, query.key, "QUERY_HIT_MESSAGE", new RMIMetadata(hostname, port +"", rmi_metadata.src_hostname, rmi_metadata.src_port));
        }
        return ACK;
	}

	@Override
	public String QUERY_HIT_MESSAGE(String clientId, String key, Object message) throws RemoteException {
		QueryHitMessage query_hit_message = (QueryHitMessage) message;
		RMIMetadata conn = rmi_super_peer_client.getDestination(query_hit_message);
        if (conn == null) {
          return "Invalid connection";
        }
        QueryHitMessage hit = (QueryHitMessage) message;
          try {
            query_hit_message.prepareForward();
            RMISuperPeerClient.forward(hit, key, "QUERY_HIT_MESSAGE", new RMIMetadata(hostname, port +"", conn.src_hostname, conn.src_port));
          } catch (MessageExpiredException e) {
          }
        return ACK;
	}

	@Override
	public String OBTAIN(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
