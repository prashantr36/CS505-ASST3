package project3.pa2;

import java.util.LinkedHashMap;
import java.util.Map;

import project3.pa1.RMIClient;

public class RMISuperPeerClient extends RMIClient {
  private LinkedHashMap<Message, RMIMetadata> seen;
  private static String my_localhost;
  private static String my_port;
  final int MAX = 1000;
  protected RMISuperPeerClient(String my_localhost, String my_port) {
	seen = new LinkedHashMap<Message, RMIMetadata>() {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected boolean removeEldestEntry(Map.Entry<Message, RMIMetadata> eldest)
        {
            return size() > MAX;
        }
    };
    RMISuperPeerClient.my_localhost = my_localhost;
    RMISuperPeerClient.my_port = my_port;
  }
  synchronized void setSeen(Message msg, RMIMetadata origin) {
    seen.put(msg, origin);
  }
  boolean haveSeen(Message msg) {
    return seen.get(msg) != null;
  }
  RMIMetadata getDestination(Message msg) {
    return seen.get(msg);
  }
  public static void main(String args[]) {
	  String clientId = System.getProperty("clientId");
	  String mod_clientId = "" + (Integer.parseInt(clientId) + 1);
	  String serverChoice = System.getProperty("serverChoice");
	  System.out.println("GNUTELLA CLUSTER " + " " + System.getProperty("clientId") + " RMIServerChoice" + serverChoice);
	  RMIClient.main(args, mod_clientId, serverChoice);
  }
  public static void forward(Object message, String clientId, String key, String command, RMIMetadata rmi_metadata) {
	  forwarder(rmi_metadata, key, "", -1, clientId, command, message);
  } 
}