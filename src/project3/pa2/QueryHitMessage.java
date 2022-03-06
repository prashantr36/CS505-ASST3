package project3.pa2;

public class QueryHitMessage extends Message {
  protected QueryHitMessage(byte[] messageId, String[] matches, String leaf_node_hostname, String leaf_node_port) {
    super(TYPE_ID);
    this.leaf_node_port = leaf_node_port;
    this.matches = matches;
    this.leaf_node_address = null;
    this.messageId = messageId;
  }
  private static final long serialVersionUID = 1L;
  protected String leaf_node_address;
  protected String leaf_node_port;
  protected String[] matches;
  protected int[] sizes;
  public static final byte TYPE_ID = (byte) 0x81;
}