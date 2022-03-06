package project3.pa2;


import java.net.InetAddress;
public class QueryHitMessage extends Message {
  protected QueryHitMessage(byte[] messageId, String[] matches, int fileServerPort) {
    super(TYPE_ID);
    this.fileServerPort = fileServerPort;
    this.matches = matches;
    this.address = null;
    this.messageId = messageId;
  }
  private static final long serialVersionUID = 1L;
  protected InetAddress address;// TODO rimettere protetti
  protected int fileServerPort;
  protected String[] matches;
  protected int[] sizes;
  public static final byte TYPE_ID = (byte) 0x81;
}