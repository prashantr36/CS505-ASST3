package project3.pa2;

import java.util.Hashtable;

import project3.pa1.RMIClient;

public class RMISuperPeerClient extends RMIClient {
  private Hashtable<Message, RMIMetadata> seen;
  protected RMISuperPeerClient() {
    seen = new Hashtable<Message, RMIMetadata>();
  }
  void setSeen(Message msg, RMIMetadata origin) {
    seen.put(msg, origin);
  }
  boolean haveSeen(Message msg) {
    return seen.get(msg) != null;
  }
  RMIMetadata getDestination(Message msg) {
    return seen.get(msg);
  }
  void removeSeen(Message msg) {
    seen.remove(msg);
  }
  public static void main(String args[]) {
	  RMIClient.main(args);
  }
}