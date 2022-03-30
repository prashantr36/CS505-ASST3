package project3.pa3;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;

import project3.pa1.RMIClient.RMIMetadata;
import project3.pa1.RMIServerInterfaceImpl;
import project3.pa3.FileRepository.FileRepositoryFile;
import project3.pa3.FileRepository.FileUnFoundException;

public class LeafNodeServerInterfaceImpl extends RMIServerInterfaceImpl implements LeafNodeServerInterface{
	private static final long serialVersionUID = 1L;
	private static FileRepository file_repository;
	protected LeafNodeServerInterfaceImpl(int portNumber) throws Exception {
		super(portNumber, (file_repository = new FileRepository("localhost", portNumber)));
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

	@SuppressWarnings("static-access")
	@Override
	public String INVALIDATION(String message_id, String clientID, String filename, int versionNumber)
			throws RemoteException {
		try {
			if(!file_repository.get(filename).isMasterClient()
					&& file_repository.get(filename).isValid()
				&& file_repository.isStaleThenInvalidate(filename, versionNumber)) {
				InvalidateMessage i_msg = new InvalidateMessage(filename, versionNumber);
				log.info("[FileRepository (" + FileRepository.hostName + "," + FileRepository.portNumber + ")| Success Cache cleared]  request from " + clientID + i_msg);
			}
		} catch (FileUnFoundException e) {
			InvalidateMessage i_msg = new InvalidateMessage(filename, versionNumber);
			//log.error("[FileRepository| Error]  request from " + clientID + " i_msg" + i_msg + " No such file in cache.");
		} catch (IOException e) {
			InvalidateMessage i_msg = new InvalidateMessage(filename, versionNumber);
			log.error("[FileRepository (" + FileRepository.hostName + "," + FileRepository.portNumber + ")| Error]  IOException request from " + clientID + "  i_msg "+ i_msg + " No such file in cache.");
		}
		return ACK;
	}

	@Override
	public String EDIT(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			log.info(" EDIT WAS CALLED FROM CLIENTID " + clientId + " Key: " + key);
			file_repository.update(new FileRepository.FileRepositoryFile(key));
			return ACK;
		} catch(Exception e) {
			e.printStackTrace();
			return NACK;
		}
	}
	
	@Override
	public String POLL(String clientId, String key, Long version) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			log.info(" POLL WAS CALLED FROM CLIENTID " + clientId + " Key: " + key);
			if(!file_repository.isValidFile(key, version)) {
				return FILE_OUTDATED;
			}
			return ACK;
		} catch(Exception e) {
			e.printStackTrace();
			return NACK;
		}
	}
	
	@Override
	public FileRepositoryFile POLL_FILE_REPOSITORY(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			log.info(" POLL FileRepositoryFile WAS CALLED FROM CLIENTID " + clientId + " Key: " + key);
			return file_repository.get(key);
		} catch(Exception e) {
			e.printStackTrace();
			return new FileRepositoryFile();
		}
	}
	
	@Override
	public String DELETE(String clientId, String key) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			log.info(" DELETE WAS CALLED FROM CLIENTID " + clientId + " Key: " + key);
			file_repository.update(new FileRepository.FileRepositoryFile(key));
			return ACK;
		} catch(Exception e) {
			log.error(e.getStackTrace());
			return NACK;
		}
	}

	@Override
	public String RETRIEVE(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}