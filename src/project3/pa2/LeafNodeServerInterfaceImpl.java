package project3.pa2;

import java.rmi.RemoteException;

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
		QueryMessage query = (QueryMessage)(message);
        if(clientId.split(":")[0].isEmpty() || clientId.split(":")[1].isEmpty()) {
			return "Error in parsing clientId" + clientId;
		}
        String dst_source = clientId.split(":")[0];
        String dst_port = clientId.split(":")[1];
        RMIMetadata rmi_metadata = new RMIMetadata(local_hostname, local_port +"", dst_source, dst_port);
        try {
          query.prepareForward();
          for(String[] coordinator_connects: this.coordinator_connections) {
        	  rmi_metadata.dst_hostname = coordinator_connects[0];
        	  rmi_metadata.dst_port = coordinator_connects[1];
        	  log.info("FORWARDING to " + rmi_metadata.dst_hostname + " : " + rmi_metadata.dst_port
        			  + " from " + clientId);
        	  RMISuperPeerClient.forward(message, query.key, "QUERY_MESSAGE", rmi_metadata);
          }
        } catch (MessageExpiredException e) {
        }
        return ACK;
	}
	
	@Override
	public String QUERY_HIT_MESSAGE(String clientId, String key, Object message) throws RemoteException{
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
		RMISuperPeerClient.forward(null, filename, "OBTAIN", rmi_metadata);
	}
}