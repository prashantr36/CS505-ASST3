package project3.pa2;

import java.rmi.RemoteException;
import java.util.Arrays;

import project3.pa1.RMIClient.RMIMetadata;
import project3.pa1.RMICoordinatorInterfaceImpl;
import project3.pa1.RMIServerInterfaceImpl;

public class LeafNodeServerInterfaceImpl extends RMIServerInterfaceImpl{
	private static final long serialVersionUID = 1L;
	protected LeafNodeServerInterfaceImpl(int portNumber) throws Exception {
		super(portNumber);
	}
	
	@Override
	public String QUERY_MESSAGE(String clientId, Object message) throws RemoteException {
		log.info("[LEAFNODE-QUERY] from " + clientId + " " + message
				+ " at " + local_hostname + " port " + local_port);
		QueryMessage query = (QueryMessage)(message);
        if(clientId.split(":")[0].isEmpty() || clientId.split(":")[1].isEmpty()) {
			return "Error in parsing clientId" + clientId;
		}
        String dst_source = clientId.split(":")[0];
        String dst_port = clientId.split(":")[1];
        RMIMetadata rmi_metadata = new RMIMetadata(local_hostname, local_port +"", dst_source, dst_port);
        try {
          query.decrementTimeToLiveCounter();
          for(String[] coordinator_connects: this.coordinator_connections) {
        	  rmi_metadata.dst_hostname = coordinator_connects[0];
        	  rmi_metadata.dst_port = coordinator_connects[1];
        	  System.out.println(" BEGIN FWD AT " + local_hostname + " " + local_port
        			   + " -> " + Arrays.toString(coordinator_connects));
        	  RMISuperPeerClient.forward(message, local_hostname + ":" + local_port, query.key, "QUERY_MESSAGE", rmi_metadata);
          }
        } catch (MessageExpiredException e) {
        }
        return ACK;
	}
	
	@Override
	public String QUERY_HIT_MESSAGE(String clientId, String key, Object message) throws RemoteException{
		log.info("[LEAFNODE-QHM] from " + clientId + " " + message
				+ " at " + local_hostname + " port " + local_port);
		QueryHitMessage query_hit_message = (QueryHitMessage) message;
		if(query_hit_message.matches.length >0) {
			String dst_host = query_hit_message.matches[0].split(":")[0];
			String dst_port = query_hit_message.matches[0].split(":")[1];
			RMIMetadata rmi_meta_data = new RMIMetadata(dst_host, dst_port, local_hostname, local_port);
			obtain(key, rmi_meta_data);
			
		}
		return ACK;
	}
	
	public void obtain(String filename, RMIMetadata rmi_metadata) {
		log.info(" RUNNIGN OBTAIN " + filename + " on RMIMetadata " + rmi_metadata);
		RMISuperPeerClient.forward(null, local_hostname + ":" + local_port,  filename, "OBTAIN", rmi_metadata);
	}
}